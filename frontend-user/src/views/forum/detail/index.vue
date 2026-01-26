<template>
  <div class="post-detail min-h-screen bg-gray-100">
    <NavBar :title="post?.title || '帖子详情'" :left-arrow="true" @click-left="onClickLeft" />

    <div v-if="post" class="post-content bg-white p-4">
      <div class="flex justify-between items-center mb-4 text-sm text-gray-500">
        <div class="post-author">作者：{{ post.userNickname }}</div>
        <div class="post-time">{{ formatDate(post.createdAt) }}</div>
      </div>
      <div class="post-body text-base leading-relaxed mb-5">{{ post.content }}</div>
      <div class="post-actions flex gap-5">
        <BaseButton
          :type="isLiked ? 'primary' : 'default'"
          size="small"
          round
          @click="onToggleLike"
        >
          {{ isLiked ? '已赞' : '赞' }} {{ post.likeCount }}
        </BaseButton>
        <BaseButton type="default" size="small" round>
          评论 {{ post.commentCount }}
        </BaseButton>
        <BaseButton
          :type="isCollected ? 'primary' : 'default'"
          size="small"
          round
          @click="onToggleCollect"
        >
          {{ isCollected ? '已收藏' : '收藏' }}
        </BaseButton>
      </div>
    </div>

    <!-- Comments Section -->
    <div class="comments-section mt-2.5 bg-white p-4">
      <div class="font-bold mb-3">评论 ({{ comments.length }})</div>

      <!-- Comment Input -->
      <div class="comment-input mb-4">
        <textarea
          v-model="newComment"
          placeholder="发表你的评论..."
          class="w-full border rounded-md px-3 py-2 text-sm"
          rows="3"
        ></textarea>
        <BaseButton
          type="primary"
          size="small"
          @click="onSubmitComment"
          :loading="submittingComment"
          class="mt-2"
        >
          发表
        </BaseButton>
      </div>

      <!-- Comment List -->
      <div class="comment-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item border-b py-3 last:border-b-0"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <div class="comment-author font-bold text-sm mb-1">
                {{ comment.userNickname }}
              </div>
              <div class="comment-content text-sm text-gray-600">
                {{ comment.content }}
              </div>
              <div class="comment-time text-xs text-gray-400 mt-1">
                {{ formatDate(comment.createdAt) }}
              </div>
            </div>
            <BaseButton
              v-if="isCommentAuthor(comment)"
              type="danger"
              size="small"
              @click="onDeleteComment(comment.id)"
            >
              删除
            </BaseButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getPostById,
  getCommentsByPost,
  createComment,
  deleteComment,
  toggleLikePost,
  toggleCollectPost,
  checkPostLiked,
  checkPostCollected
} from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const newComment = ref('')
const submittingComment = ref(false)

async function loadPostDetail() {
  try {
    const postId = route.params.id
    post.value = await getPostById(postId)

    // 并行加载状态和评论
    const [liked, collected, commentList] = await Promise.all([
      checkPostLiked(postId),
      checkPostCollected(postId),
      getCommentsByPost(postId, { page: 1, size: 50 })
    ])

    isLiked.value = liked
    isCollected.value = collected
    comments.value = commentList.records || commentList
  } catch (error) {
    console.error('获取帖子详情失败:', error)
  }
}

async function onToggleLike() {
  try {
    const result = await toggleLikePost(route.params.id)
    isLiked.value = result.isLiked
    post.value.likeCount = result.likeCount
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

async function onToggleCollect() {
  try {
    const result = await toggleCollectPost(route.params.id)
    isCollected.value = result.isCollected
    post.value.collectCount = result.collectCount
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

async function onSubmitComment() {
  if (!newComment.value.trim()) return

  submittingComment.value = true
  try {
    const comment = await createComment({
      postId: route.params.id,
      content: newComment.value
    })
    comments.value.unshift(comment)
    post.value.commentCount++
    newComment.value = ''
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    submittingComment.value = false
  }
}

async function onDeleteComment(commentId) {
  try {
    await deleteComment(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
    post.value.commentCount--
  } catch (error) {
    console.error('删除评论失败:', error)
  }
}

function isCommentAuthor(comment) {
  return comment.userId === userStore.userInfo?.id
}

function onClickLeft() {
  router.back()
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadPostDetail()
})
</script>
