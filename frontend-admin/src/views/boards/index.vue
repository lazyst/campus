<template>
  <div class="board-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>板块管理</span>
          <el-button type="primary" @click="handleCreate">新建板块</el-button>
        </div>
      </template>

      <el-table :data="boardList" v-loading="loading" stripe style="width: 100%">
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
        <el-table-column label="操作" width="200">
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
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
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
const pageSizes = [10, 20, 50]
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
    boardList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取板块列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
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

  :deep(.el-card) {
    background: #FFFFFF;
    border: 1px solid #E5E7EB;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #F3F4F6;
    background: #FAFAFA;
    color: #374151;
    font-size: 15px;
    font-weight: 500;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  :deep(.el-table) {
    background: transparent;

    &::before {
      display: none;
    }

    tr {
      background: transparent;
    }

    th.el-table__cell {
      background: #F9FAFB;
      color: #374151;
      font-weight: 600;
      border-bottom: 1px solid #E5E7EB;
    }

    td.el-table__cell {
      border-bottom: 1px solid #F3F4F6;
      color: #374151;
    }

    .el-table__row:hover > td.el-table__cell {
      background: #F3F4F6 !important;
    }

    .el-table__row:nth-child(even) > td.el-table__cell {
      background: #FAFAFA;
    }
  }

  :deep(.el-button--primary) {
    background: #1E3A8A;
    border: none;
    border-radius: 8px;
    font-weight: 500;

    &:hover {
      background: #1E40AF;
    }
  }

  :deep(.el-button--danger) {
    background: #FEE2E2;
    border: 1px solid #FECACA;
    color: #991B1B;
    border-radius: 6px;

    &:hover {
      background: #FECACA;
    }
  }

  :deep(.el-tag) {
    border-radius: 6px;
    border: none;
    font-weight: 500;
  }

  :deep(.el-tag--success) {
    background: #DCFCE7;
    color: #166534;
  }

  :deep(.el-tag--danger) {
    background: #FEE2E2;
    color: #991B1B;
  }

  :deep(.el-pagination) {
    color: #6B7280;

    .el-pager li {
      background: #FFFFFF;
      border: 1px solid #E5E7EB;
      color: #374151;
      border-radius: 6px;
      margin: 0 2px;

      &:hover {
        color: #1E3A8A;
        border-color: #1E3A8A;
      }

      &.is-active {
        background: #1E3A8A;
        border-color: #1E3A8A;
        color: #FFFFFF;
      }
    }

    .btn-prev,
    .btn-next {
      background: #FFFFFF;
      border: 1px solid #E5E7EB;
      color: #374151;
      border-radius: 6px;

      &:hover {
        color: #1E3A8A;
        border-color: #1E3A8A;
      }
    }
  }

  :deep(.el-dialog) {
    border-radius: 16px;
    background: #FFFFFF;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);

    .el-dialog__header {
      padding: 20px 24px;
      border-bottom: 1px solid #E5E7EB;
    }

    .el-dialog__title {
      color: #111827;
      font-weight: 600;
    }

    .el-dialog__body {
      padding: 24px;
      color: #374151;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid #E5E7EB;
    }
  }

  :deep(.el-form-item__label) {
    color: #374151;
    font-weight: 500;
  }

  :deep(.el-input__wrapper) {
    background: #FFFFFF !important;
    border: 1px solid #D1D5DB !important;
    box-shadow: none !important;
    border-radius: 8px;

    &:hover {
      border-color: #9CA3AF !important;
    }

    &.is-focus {
      border-color: #1E3A8A !important;
      box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1) !important;
    }
  }

  :deep(.el-input__inner) {
    color: #111827 !important;

    &::placeholder {
      color: #9CA3AF !important;
    }
  }

  :deep(.el-textarea__inner) {
    background: #FFFFFF !important;
    border: 1px solid #D1D5DB !important;
    color: #111827 !important;
    border-radius: 8px;

    &::placeholder {
      color: #9CA3AF !important;
    }
  }

  :deep(.el-input-number) {
    .el-input-number__decrease,
    .el-input-number__increase {
      background: #F9FAFB;
      border: 1px solid #E5E7EB;
      color: #6B7280;

      &:hover {
        color: #1E3A8A;
      }
    }
  }

  :deep(.el-button--default) {
    background: #FFFFFF;
    border: 1px solid #D1D5DB;
    color: #374151;
    border-radius: 8px;

    &:hover {
      background: #F9FAFB;
      border-color: #9CA3AF;
    }
  }
}
</style>
