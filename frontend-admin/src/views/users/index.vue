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

      <div class="table-container">
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
              <div class="action-buttons">
                <el-button v-if="row.status === 1" type="warning" size="small" @click="handleBan(row)">封禁</el-button>
                <el-button v-else type="success" size="small" @click="handleUnban(row)">解封</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

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
import { getUserList, banUser, unbanUser, deleteUser, type User } from '@/api/admin/user'
import { usePagination } from '@/composables/usePagination'
import { useCrud } from '@/composables/useCrud'

const { loading, currentPage, pageSize, pageSizes, total, handleCurrentChange, handleSizeChange } = usePagination()
const { handleToggleStatus, handleDelete: doDelete } = useCrud()

const userList = ref<User[]>([])
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
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleBan = async (user: User) => {
  await handleToggleStatus('用户', 'ban', banUser, { id: user.id }, fetchData)
}

const handleUnban = async (user: User) => {
  await handleToggleStatus('用户', 'unban', unbanUser, { id: user.id }, fetchData)
}

const handleDelete = async (user: User) => {
  await doDelete('用户', deleteUser, { id: user.id }, fetchData)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
@import "@/styles/index.scss";

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

    @media (max-width: 480px) {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }
  }

  // 搜索区域
  .card-header {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;
    }

    :deep(.el-input),
    :deep(.el-select) {
      @media (max-width: 768px) {
        width: 100% !important;
        margin-left: 0 !important;
      }
    }

    :deep(.el-button) {
      @media (max-width: 768px) {
        width: 100%;
        margin-left: 0 !important;
      }
    }
  }

  // 表格容器 - 允许水平滚动
  .table-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  // 操作按钮 - 移动端堆叠
  .action-buttons {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;

    @media (max-width: 480px) {
      flex-direction: column;
      
      .el-button {
        width: 100%;
        margin-left: 0 !important;
        margin-bottom: 4px;
      }
    }
  }

  // 表格容器
  @include page-styles;

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
