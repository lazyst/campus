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
          <el-select v-model="sortBy" placeholder="排序" style="width: 150px; margin-left: 10px" @change="handleSearch">
            <el-option label="最新发布" value="createdAt_desc" />
            <el-option label="最早发布" value="createdAt_asc" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="浏览从低到高" value="viewCount_asc" />
            <el-option label="浏览从高到低" value="viewCount_desc" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </template>

      <el-table :data="itemList" v-loading="loading" stripe style="width: 100%">
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
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button v-if="row.status === 3" type="success" size="small" @click="handleOnline(row)">上架</el-button>
              <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOffline(row)">下架</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
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

    <!-- 闲置物品详情对话框 -->
    <el-dialog v-model="detailVisible" title="闲置物品详情" width="720px">
      <div class="item-detail" v-if="currentItem">
        <div class="item-card">
          <div class="item-header">
            <h2 class="item-title">{{ currentItem.title }}</h2>
            <div class="item-meta">
              <span class="meta-item">发布时间: {{ currentItem.createdAt }}</span>
              <span class="meta-item">浏览: {{ currentItem.viewCount }}</span>
            </div>
          </div>

          <div class="item-price">
            <span class="price-label">价格:</span>
            <span class="price-value">¥{{ currentItem.price }}</span>
          </div>

          <div class="item-info">
            <el-tag :type="currentItem.type === 1 ? 'warning' : 'success'" style="margin-right: 10px">
              {{ currentItem.type === 1 ? '收购' : '出售' }}
            </el-tag>
            <el-tag :type="getStatusType(currentItem.status)">
              {{ getStatusText(currentItem.status) }}
            </el-tag>
          </div>

          <div class="seller-info">
            <div class="seller-avatar">卖</div>
            <span class="seller-name">{{ currentItem.userNickname || '匿名用户' }}</span>
          </div>

          <div class="item-description">
            <h3 class="description-title">物品描述</h3>
            <p>{{ currentItem.description || '暂无描述' }}</p>
          </div>

          <div class="item-images" v-if="currentItem.images && currentItem.images.length > 0">
            <h3 class="images-title">物品图片</h3>
            <div class="image-list">
              <div class="image-item" v-for="(img, index) in currentItem.images" :key="index" @click="previewImage(index)">
                <img :src="img" :alt="'图片' + (index + 1)" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button v-if="currentItem?.status === 3" type="success" @click="handleOnlineInDialog">上架物品</el-button>
          <el-button v-if="currentItem?.status === 1" type="warning" @click="handleOfflineInDialog">下架物品</el-button>
          <el-button type="danger" @click="handleDeleteItem">删除物品</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="currentItem?.images || []"
      :initial-index="currentImageIndex"
      @close="showImageViewer = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getItemList, getItemDetail, deleteItem, offlineItem, onlineItem, type Item } from '@/api/admin/item'

const loading = ref(false)
const itemList = ref<Item[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const pageSizes = [10, 20, 50, 100]
const total = ref(0)
const searchKeyword = ref('')
const searchType = ref<number | null>(null)
const searchStatus = ref<number | null>(null)
const sortBy = ref('createdAt_desc')

const detailVisible = ref(false)
const currentItem = ref<Item | null>(null)
const showImageViewer = ref(false)
const currentImageIndex = ref(0)

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
      status: searchStatus.value || undefined,
      sortBy: sortBy.value
    })
    itemList.value = res.data.records
    total.value = res.data.total
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

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleView = async (item: Item) => {
  try {
    const res = await getItemDetail(item.id)
    const detail = res.data
    if (detail.images) {
      try {
        detail.images = JSON.parse(detail.images)
      } catch {
        detail.images = []
      }
    }
    currentItem.value = detail
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取物品详情失败')
  }
}

const previewImage = (index: number) => {
  currentImageIndex.value = index
  showImageViewer.value = true
}

const handleOnline = async (item: Item) => {
  try {
    await ElMessageBox.confirm('确定要上架该物品吗？', '提示', { type: 'warning' })
    await onlineItem(item.id)
    ElMessage.success('上架成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('上架失败')
  }
}

const handleOnlineInDialog = async () => {
  if (!currentItem.value) return
  try {
    await ElMessageBox.confirm('确定要上架该物品吗？', '提示', { type: 'warning' })
    await onlineItem(currentItem.value.id)
    ElMessage.success('上架成功')
    detailVisible.value = false
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('上架失败')
  }
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

const handleOfflineInDialog = async () => {
  if (!currentItem.value) return
  try {
    await ElMessageBox.confirm('确定要下架该物品吗？', '提示', { type: 'warning' })
    await offlineItem(currentItem.value.id)
    ElMessage.success('下架成功')
    detailVisible.value = false
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

const handleDeleteItem = async () => {
  if (!currentItem.value) return
  try {
    await ElMessageBox.confirm('确定要删除该物品吗？此操作不可恢复！', '警告', { type: 'error' })
    await deleteItem(currentItem.value.id)
    ElMessage.success('删除成功')
    detailVisible.value = false
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

  .action-buttons {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;
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

  :deep(.el-tag) {
    border-radius: 6px;
    border: none;
    font-weight: 500;
  }

  :deep(.el-tag--success) {
    background: #DCFCE7;
    color: #166534;
  }

  :deep(.el-tag--warning) {
    background: #FEF3C7;
    color: #92400E;
  }

  :deep(.el-tag--info) {
    background: #F3F4F6;
    color: #4B5563;
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

    .el-pagination__jump {
      color: #6B7280;
    }
  }
}

// 闲置物品详情对话框
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

.item-detail {
  padding: 0;
}

.item-card {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  padding: 24px;
}

.item-header {
  margin-bottom: 16px;

  .item-title {
    font-size: 20px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .item-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .meta-item {
      font-size: 13px;
      color: #6B7280;
    }
  }
}

.item-price {
  margin-bottom: 16px;

  .price-label {
    font-size: 14px;
    color: #6B7280;
    margin-right: 8px;
  }

  .price-value {
    font-size: 24px;
    font-weight: 700;
    color: #DC2626;
  }
}

.item-info {
  margin-bottom: 16px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 8px;

  .seller-avatar {
    width: 32px;
    height: 32px;
    background: #1E3A8A;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 12px;
    font-weight: 600;
  }

  .seller-name {
    font-size: 14px;
    font-weight: 500;
    color: #1E3A8A;
  }
}

.item-description {
  margin-bottom: 20px;

  .description-title {
    font-size: 16px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 12px 0;
  }

  p {
    font-size: 15px;
    line-height: 1.8;
    color: #374151;
    white-space: pre-wrap;
    word-break: break-word;
    margin: 0;
  }
}

.item-images {
  .images-title {
    font-size: 16px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 12px 0;
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .image-item {
    width: 140px;
    height: 140px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid #E5E7EB;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    &:hover img {
      transform: scale(1.05);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-button--success) {
  background: #DCFCE7;
  border: 1px solid #86EFAC;
  color: #166534;
  border-radius: 6px;

  &:hover {
    background: #86EFAC;
  }
}
</style>
