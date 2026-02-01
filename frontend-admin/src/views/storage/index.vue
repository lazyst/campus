<template>
  <div class="storage-management">
    <!-- 存储统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-icon total-files">
            <el-icon :size="28"><Picture /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalFiles || 0 }}</div>
            <div class="stat-label">总文件数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-icon used-files">
            <el-icon :size="28"><FolderOpened /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.usedFiles || 0 }}</div>
            <div class="stat-label">已使用文件</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-icon unused-files">
            <el-icon :size="28"><Delete /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.unusedFiles || 0 }}</div>
            <div class="stat-label">未使用文件</div>
          </div>
          <div class="stat-extra" v-if="stats.unusedFiles > 0">
            <el-button type="primary" size="small" @click="handleCleanUnused">一键清理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-icon storage-space">
            <el-icon :size="28"><Folder /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.formattedSize || '0 B' }}</div>
            <div class="stat-label">总存储空间</div>
          </div>
          <div class="stat-extra">
            <el-button :icon="Refresh" circle @click="fetchStats" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容区域 -->
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部文件" name="all" />
            <el-tab-pane label="未使用图片" name="unused" />
          </el-tabs>
          <div class="header-actions">
            <el-select v-model="selectedDateDir" placeholder="日期目录" clearable style="width: 150px" @change="handleSearch">
              <el-option label="全部" value="" />
              <el-option v-for="dir in dateDirs" :key="dir" :label="dir" :value="dir" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件名"
              style="width: 200px; margin-left: 10px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" style="margin-left: 10px" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button
              v-if="selectedImages.length > 0"
              type="danger"
              style="margin-left: 10px"
              :icon="Delete"
              @click="handleBatchDelete"
            >
              批量删除 ({{ selectedImages.length }})
            </el-button>
          </div>
        </div>
      </template>

      <!-- 图片网格视图 -->
      <div v-loading="loading" class="image-grid">
        <el-empty v-if="!loading && fileList.length === 0" description="暂无文件" />

        <el-row :gutter="16" v-else>
          <el-col
            v-for="file in fileList"
            :key="file.url"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
          >
            <div
              class="image-card"
              :class="{ selected: selectedImages.includes(file.url) }"
              @click="handleSelectImage(file.url)"
            >
              <div class="image-wrapper">
                <el-image
                  :src="file.url"
                  :preview-src-list="previewList"
                  :initial-index="previewList.indexOf(file.url)"
                  fit="cover"
                  class="image-preview"
                />
                <div class="image-overlay">
                  <el-button
                    type="primary"
                    circle
                    :icon="View"
                    size="small"
                    title="预览"
                    @click.stop
                  />
                  <el-button
                    type="danger"
                    circle
                    :icon="Delete"
                    size="small"
                    title="删除"
                    @click.stop="handleDelete(file)"
                  />
                </div>
                <div class="select-checkbox" v-if="activeTab === 'unused'">
                  <el-checkbox
                    :model-value="selectedImages.includes(file.url)"
                    @update:model-value="handleSelectImage(file.url)"
                    @click.stop
                  />
                </div>
              </div>
              <div class="image-info">
                <el-tooltip :content="file.fileName" placement="top" :show-after="500">
                  <div class="filename">{{ file.fileName }}</div>
                </el-tooltip>
                <div class="file-meta">
                  <span>{{ file.formattedSize || formatSize(file.fileSize) }}</span>
                  <span>{{ formatDate(file.createTime) }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 48, 96]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture,
  FolderOpened,
  Delete,
  Folder,
  Refresh,
  Search,
  View
} from '@element-plus/icons-vue'
import {
  getStorageStats,
  getFileList,
  getDateDirs,
  getUnusedImages,
  deleteImage,
  deleteImages,
  cleanUnusedImages
} from '@/api/admin/storage'

const stats = ref({
  totalFiles: 0,
  usedFiles: 0,
  unusedFiles: 0,
  totalSize: 0,
  formattedSize: '0 B'
})

const loading = ref(false)
const fileList = ref([])
const dateDirs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')
const selectedDateDir = ref('')
const activeTab = ref('all')
const selectedImages = ref([])
const previewList = computed(() => fileList.value.map(f => f.url))

const fetchStats = async () => {
  try {
    const res = await getStorageStats()
    stats.value = res
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchDateDirs = async () => {
  try {
    const res = await getDateDirs()
    dateDirs.value = res || []
  } catch (error) {
    console.error('获取日期目录失败:', error)
  }
}

const fetchFileList = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'all') {
      res = await getFileList({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value || undefined,
        dateDir: selectedDateDir.value || undefined
      })
    } else {
      res = await getUnusedImages({
        pageNum: currentPage.value,
        pageSize: pageSize.value
      })
    }
    fileList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  selectedImages.value = []
  fetchFileList()
}

const handleTabChange = () => {
  currentPage.value = 1
  selectedImages.value = []
  searchKeyword.value = ''
  selectedDateDir.value = ''
  fetchFileList()
}

const handleSelectImage = (url) => {
  const index = selectedImages.value.indexOf(url)
  if (index > -1) {
    selectedImages.value.splice(index, 1)
  } else {
    selectedImages.value.push(url)
  }
}

const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.fileName}" 吗？`, '确认删除', {
      type: 'warning'
    })
    await deleteImage(file.url)
    ElMessage.success('删除成功')
    fetchFileList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedImages.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedImages.value.length} 个文件吗？`,
      '批量删除',
      { type: 'warning' }
    )
    await deleteImages(selectedImages.value)
    ElMessage.success(`成功删除 ${selectedImages.value.length} 个文件`)
    selectedImages.value = []
    fetchFileList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleCleanUnused = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清理所有未使用的图片吗？预计可清理 ${stats.value.unusedFiles} 个文件。`,
      '清理未使用图片',
      { type: 'warning' }
    )
    const result = await cleanUnusedImages()
    ElMessage.success(`清理成功！共清理 ${result.deletedCount || stats.value.unusedFiles} 个文件`)
    fetchStats()
    if (activeTab.value === 'unused') {
      fetchFileList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理失败')
    }
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return ''
  }
}

onMounted(() => {
  fetchStats()
  fetchDateDirs()
  fetchFileList()
})
</script>

<style scoped lang="scss">
.storage-management {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    :deep(.el-card) {
      background: #FFFFFF;
      border: 1px solid #E5E7EB;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
      position: relative;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;

      &.total-files {
        background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
        color: #4F46E5;
      }

      &.used-files {
        background: linear-gradient(135deg, #DCFCE7 0%, #BBF7D0 100%);
        color: #16A34A;
      }

      &.unused-files {
        background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
        color: #DC2626;
      }

      &.storage-space {
        background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
        color: #D97706;
      }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #111827;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 14px;
        color: #6B7280;
        margin-top: 4px;
      }
    }

    .stat-extra {
      position: absolute;
      top: 16px;
      right: 16px;
    }
  }

  .content-card {
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
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;

      :deep(.el-tabs) {
        .el-tabs__item {
          font-size: 15px;
          color: #6B7280;

          &.is-active {
            color: #1E3A8A;
            font-weight: 600;
          }
        }

        .el-tabs__active-bar {
          background: #1E3A8A;
        }
      }

      .header-actions {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
      }
    }
  }

  .image-grid {
    min-height: 300px;

    .image-card {
      background: #FFFFFF;
      border: 2px solid #E5E7EB;
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        border-color: #1E3A8A;
        box-shadow: 0 4px 12px rgba(30, 58, 138, 0.15);
        transform: translateY(-2px);

        .image-overlay {
          opacity: 1;
        }
      }

      &.selected {
        border-color: #1E3A8A;
        background: #EEF2FF;
        box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.2);
      }

      .image-wrapper {
        position: relative;
        height: 140px;
        background: #F9FAFB;
        overflow: hidden;

        .image-preview {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        .select-checkbox {
          position: absolute;
          top: 8px;
          left: 8px;
          z-index: 10;
        }
      }

      .image-info {
        padding: 12px;

        .filename {
          font-size: 13px;
          color: #374151;
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-bottom: 6px;
        }

        .file-meta {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #9CA3AF;
        }
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
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

  :deep(.el-button--danger) {
    background: #FEE2E2;
    border: 1px solid #FECACA;
    color: #991B1B;
    border-radius: 8px;
    font-weight: 500;

    &:hover {
      background: #FECACA;
    }
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

  :deep(.el-checkbox) {
    --el-checkbox-checked-bg-color: #1E3A8A;
    --el-checkbox-checked-input-border-color: #1E3A8A;
  }

  :deep(.el-empty) {
    padding: 40px 0;

    .el-empty__description p {
      color: #6B7280;
      font-size: 14px;
    }
  }
}
</style>
