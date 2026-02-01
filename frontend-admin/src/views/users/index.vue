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

      <el-table :data="userList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="phone" label="手机号" width="110" />
        <el-table-column prop="nickname" label="昵称" min-width="80" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="150" />
        <el-table-column label="操作" width="180">
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
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
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
const pageSizes = [10, 20, 50, 100]
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
    userList.value = res.data.records
    total.value = res.data.total
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

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
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
  // 搜索区域
  .card-header {
    display: flex;
    align-items: center;
  }

  // 分页容器
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  // 表格容器
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

  // 表格样式
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

  // 输入框覆盖
  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
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

  // 按钮覆盖
  :deep(.el-button--primary) {
    background: #1E3A8A;
    border: none;
    border-radius: 8px;
    font-weight: 500;

    &:hover {
      background: #1E40AF;
    }
  }

  :deep(.el-button--warning) {
    background: #FEF3C7;
    border: 1px solid #FCD34D;
    color: #92400E;
    border-radius: 6px;

    &:hover {
      background: #FDE68A;
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

  :deep(.el-button--success) {
    background: #DCFCE7;
    border: 1px solid #86EFAC;
    color: #166534;
    border-radius: 6px;

    &:hover {
      background: #BBF7D0;
    }
  }

  // 标签覆盖
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

  // 分页覆盖
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

    .el-pagination__jump {
      color: #6B7280;
    }
  }
}
</style>
