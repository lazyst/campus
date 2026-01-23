<template>
  <div class="board-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>板块管理</span>
          <el-button type="primary" @click="handleCreate">新建板块</el-button>
        </div>
      </template>

      <el-table :data="boardList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="板块名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入板块描述" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getBoardList, createBoard, updateBoard, deleteBoard, type Board, type CreateBoardRequest, type UpdateBoardRequest } from '@/api/admin/board'

const loading = ref(false)
const boardList = ref<Board[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新建板块')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<CreateBoardRequest>({
  name: '',
  description: '',
  icon: '',
  sort: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入板块名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBoardList({ page: currentPage.value, size: pageSize.value })
    boardList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取板块列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新建板块'
  editingId.value = null
  form.name = ''
  form.description = ''
  form.icon = ''
  form.sort = 0
  dialogVisible.value = true
}

const handleEdit = (board: Board) => {
  dialogTitle.value = '编辑板块'
  editingId.value = board.id
  form.name = board.name
  form.description = board.description || ''
  form.icon = board.icon || ''
  form.sort = board.sort
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (editingId.value) {
        await updateBoard(editingId.value, form as UpdateBoardRequest)
        ElMessage.success('更新成功')
      } else {
        await createBoard(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const handleDelete = async (board: Board) => {
  try {
    await ElMessageBox.confirm('确定要删除该板块吗？', '警告', { type: 'error' })
    await deleteBoard(board.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.board-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
