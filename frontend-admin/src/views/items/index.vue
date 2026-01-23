<template>
  <div class="item-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-input v-model="searchKeyword" placeholder="搜索物品标题/描述" style="width: 250px" clearable @keyup.enter="handleSearch" />
          <el-select v-model="searchType" placeholder="类型筛选" clearable style="width: 120px; margin-left: 10px">
            <el-option label="收购" :value="1" />
            <el-option label="出售" :value="2" />
          </el-select>
          <el-select v-model="searchStatus" placeholder="状态筛选" clearable style="width: 120px; margin-left: 10px">
            <el-option label="在售" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已下架" :value="3" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </template>

      <el-table :data="itemList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'warning' : 'success'">
              {{ row.type === 1 ? '收购' : '出售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="userNickname" label="发布者" width="120" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOffline(row)">下架</el-button>
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
import { getItemList, deleteItem, offlineItem, type Item } from '@/api/admin/item'

const loading = ref(false)
const itemList = ref<Item[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')
const searchType = ref<number | null>(null)
const searchStatus = ref<number | null>(null)

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    1: '在售',
    2: '已完成',
    3: '已下架'
  }
  return map[status] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getItemList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      type: searchType.value || undefined,
      status: searchStatus.value || undefined
    })
    itemList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取物品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleOffline = async (item: Item) => {
  try {
    await ElMessageBox.confirm('确定要下架该物品吗？', '提示', { type: 'warning' })
    await offlineItem(item.id)
    ElMessage.success('下架成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('下架失败')
  }
}

const handleDelete = async (item: Item) => {
  try {
    await ElMessageBox.confirm('确定要删除该物品吗？此操作不可恢复！', '警告', { type: 'error' })
    await deleteItem(item.id)
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
.item-management {
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
