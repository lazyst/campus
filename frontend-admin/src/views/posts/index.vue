<template>
  <div class="post-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-input v-model="searchKeyword" placeholder="搜索帖子标题/内容" style="width: 250px" clearable @keyup.enter="handleSearch" />
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </template>

      <el-table :data="postList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
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

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="720px">
      <div class="post-detail" v-if="currentPost">
        <div class="post-card">
          <div class="post-header">
            <h2 class="post-title">{{ currentPost.title }}</h2>
            <div class="post-meta">
              <span class="meta-item">发布时间: {{ currentPost.createdAt }}</span>
              <span class="meta-item">浏览: {{ currentPost.viewCount }}</span>
              <span class="meta-item">点赞: {{ currentPost.likeCount }}</span>
              <span class="meta-item">评论: {{ currentPost.commentCount }}</span>
            </div>
          </div>

          <div class="author-info">
            <div class="author-avatar">作</div>
            <span class="author-name">{{ currentPost.userNickname || '匿名用户' }}</span>
          </div>

          <div class="post-content">
            <p>{{ currentPost.content }}</p>
          </div>

          <div class="post-images" v-if="currentPost.images && currentPost.images.length > 0">
            <div class="image-list">
              <div class="image-item" v-for="(img, index) in currentPost.images" :key="index" @click="previewImage(index)">
                <img :src="img" :alt="'图片' + (index + 1)" />
              </div>
            </div>
          </div>
        </div>

        <div class="comments-section">
          <div class="comments-header">
            <h3 class="comments-title">评论列表</h3>
            <span class="comments-count">{{ comments.length }}</span>
          </div>

          <div class="comment-list" v-if="comments.length > 0">
            <div class="comment-item" v-for="comment in comments" :key="comment.id">
              <div class="comment-avatar">评</div>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-author">{{ comment.userNickname || '匿名用户' }}</span>
                  <span class="comment-time">{{ comment.createdAt }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
              <div class="comment-actions">
                <el-button type="danger" size="small" @click="handleDeleteComment(comment)">删除</el-button>
              </div>
            </div>
          </div>
          <div class="no-comments" v-else>
            <span>暂无评论</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="danger" @click="handleDeletePost">删除帖子</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="currentPost?.images || []"
      :initial-index="currentImageIndex"
      @close="showImageViewer = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, getPostDetail, deletePost, getPostComments, deleteComment, type Post } from '@/api/admin/post'

const loading = ref(false)
const postList = ref<Post[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const pageSizes = [10, 20, 50, 100]
const total = ref(0)
const searchKeyword = ref('')

const detailVisible = ref(false)
const currentPost = ref<Post | null>(null)
const comments = ref<any[]>([])
const showImageViewer = ref(false)
const currentImageIndex = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPostList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    postList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
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

const handleView = async (post: Post) => {
  try {
    const detail = await getPostDetail(post.id)
    if (detail.images) {
      try {
        detail.images = JSON.parse(detail.images)
      } catch {
        detail.images = []
      }
    }
    currentPost.value = detail

    const commentList = await getPostComments(post.id)
    comments.value = commentList.filter((c: any) => c.status === 1)

    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取帖子详情失败')
  }
}

const previewImage = (index: number) => {
  currentImageIndex.value = index
  showImageViewer.value = true
}

const handleDelete = async (post: Post) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？此操作不可恢复！', '警告', { type: 'error' })
    await deletePost(post.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleDeletePost = async () => {
  if (!currentPost.value) return
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？此操作不可恢复！', '警告', { type: 'error' })
    await deletePost(currentPost.value.id)
    ElMessage.success('删除成功')
    detailVisible.value = false
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleDeleteComment = async (comment: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '提示', { type: 'warning' })
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    if (currentPost.value) {
      const commentList = await getPostComments(currentPost.value.id)
      comments.value = commentList.filter((c: any) => c.status === 1)
      currentPost.value.commentCount = comments.value.length
    }
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.post-management {
  .card-header {
    display: flex;
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
}

// 帖子详情对话框
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

.post-detail {
  padding: 0;
}

.post-card {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.post-header {
  margin-bottom: 16px;

  .post-title {
    font-size: 20px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .post-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .meta-item {
      font-size: 13px;
      color: #6B7280;
    }
  }
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;

  .author-avatar {
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

  .author-name {
    font-size: 14px;
    font-weight: 500;
    color: #1E3A8A;
  }
}

.post-content {
  font-size: 15px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;

  p {
    margin: 0;
  }
}

.post-images {
  margin-top: 20px;

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

.comments-section {
  .comments-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;

    .comments-title {
      font-size: 16px;
      font-weight: 600;
      color: #111827;
      margin: 0;
    }

    .comments-count {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 24px;
      height: 24px;
      padding: 0 8px;
      background: #1E3A8A;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
    }
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .comment-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    background: #F9FAFB;
    border: 1px solid #E5E7EB;
    border-radius: 12px;

    .comment-avatar {
      width: 36px;
      height: 36px;
      flex-shrink: 0;
      background: #EEF2FF;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1E3A8A;
      font-size: 12px;
      font-weight: 600;
    }

    .comment-body {
      flex: 1;
      min-width: 0;
    }

    .comment-meta {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 6px;

      .comment-author {
        font-size: 14px;
        font-weight: 500;
        color: #1E3A8A;
      }

      .comment-time {
        font-size: 12px;
        color: #9CA3AF;
      }
    }

    .comment-content {
      font-size: 14px;
      line-height: 1.6;
      color: #374151;
      word-break: break-word;
    }

    .comment-actions {
      flex-shrink: 0;
    }
  }

  .no-comments {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 40px 20px;
    color: #9CA3AF;

    span {
      font-size: 14px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
