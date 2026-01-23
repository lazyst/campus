<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" style="width: 250px" clearable @keyup.enter="handleSearch" />
          <el-select v-model="searchStatus" placeholder="状态筛选" clearable style="width: 150px; margin-left: 10px">
            <el-option label="正常" :value="1" />
            <el-option label="已封禁" :value="0" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" size="small" @click="handleBan(row)">封禁</el-button>
            <el-button v-else type="success" size="small" @click="handleUnban(row)">解封</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, banUser, unbanUser, deleteUser, type User } from '@/api/admin/user'

const loading = ref(false)
const userList = ref<User[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: searchStatus.value || undefined
    })
    userList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleBan = async (user: User) => {
  try {
    await ElMessageBox.confirm('确定要封禁该用户吗？', '提示', { type: 'warning' })
    await banUser(user.id)
    ElMessage.success('封禁成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('封禁失败')
  }
}

const handleUnban = async (user: User) => {
  try {
    await ElMessageBox.confirm('确定要解封该用户吗？', '提示', { type: 'warning' })
    await unbanUser(user.id)
    ElMessage.success('解封成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('解封失败')
  }
}

const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复！', '警告', { type: 'error' })
    await deleteUser(user.id)
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
.user-management {
  .card-header {
    display: flex;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
