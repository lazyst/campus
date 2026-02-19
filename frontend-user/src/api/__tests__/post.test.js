// frontend-user/src/api/__tests__/post.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getPosts,
  getPostById,
  getMyPosts,
  createPost,
  updatePost,
  deletePost,
  toggleLikePost,
  checkPostLiked,
  toggleCollectPost,
  checkPostCollected
} from '../modules/post'

const { mockRequest } = vi.hoisted(() => ({
  mockRequest: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

vi.mock('../request', () => ({
  default: mockRequest
}))

describe('Post API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getPosts()', () => {
    it('should get posts list with pagination', async () => {
      const mockPosts = {
        records: [
          { id: 1, title: '帖子1', content: '内容1' },
          { id: 2, title: '帖子2', content: '内容2' }
        ],
        total: 10
      }

      mockRequest.get.mockResolvedValue(mockPosts)

      const result = await getPosts({ boardId: 1, page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(result.total).toBe(10)
      expect(mockRequest.get).toHaveBeenCalledWith('/posts', { params: { boardId: 1, page: 1, size: 10 }, showLoading: false })
    })

    it('should get all posts without filters', async () => {
      mockRequest.get.mockResolvedValue({
        records: [{ id: 1, title: '帖子1' }],
        total: 1
      })

      const result = await getPosts()

      expect(result.records).toHaveLength(1)
      expect(mockRequest.get).toHaveBeenCalledWith('/posts', { params: {}, showLoading: false })
    })
  })

  describe('getPostById()', () => {
    it('should get post detail by id', async () => {
      const mockPost = {
        id: 1,
        title: '测试帖子',
        content: '这是测试内容',
        viewCount: 100,
        likeCount: 10,
        commentCount: 5
      }

      mockRequest.get.mockResolvedValue(mockPost)

      const result = await getPostById(1)

      expect(result.id).toBe(1)
      expect(result.title).toBe('测试帖子')
      expect(mockRequest.get).toHaveBeenCalledWith('/posts/1')
    })
  })

  describe('getMyPosts()', () => {
    it('should get current user posts', async () => {
      const mockPosts = {
        records: [
          { id: 1, title: '我的帖子1' },
          { id: 2, title: '我的帖子2' }
        ],
        total: 2
      }

      mockRequest.get.mockResolvedValue(mockPosts)

      const result = await getMyPosts({ page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/posts/my', { params: { page: 1, size: 10 } })
    })
  })

  describe('createPost()', () => {
    it('should create a new post successfully', async () => {
      const newPostData = {
        boardId: 1,
        title: '新帖子',
        content: '新帖子内容'
      }

      const createdPost = {
        id: 1,
        ...newPostData,
        createdAt: '2026-01-27T10:00:00'
      }

      mockRequest.post.mockResolvedValue(createdPost)

      const result = await createPost(newPostData)

      expect(result.id).toBe(1)
      expect(result.title).toBe('新帖子')
      expect(mockRequest.post).toHaveBeenCalledWith('/posts', newPostData, { loadingText: '发布中...' })
    })
  })

  describe('updatePost()', () => {
    it('should update post successfully', async () => {
      const updateData = {
        title: '更新后的标题',
        content: '更新后的内容'
      }

      const updatedPost = {
        id: 1,
        ...updateData
      }

      mockRequest.put.mockResolvedValue(updatedPost)

      const result = await updatePost(1, updateData)

      expect(result.title).toBe('更新后的标题')
      expect(mockRequest.put).toHaveBeenCalledWith('/posts/1', updateData, { loadingText: '更新中...' })
    })
  })

  describe('deletePost()', () => {
    it('should delete post successfully', async () => {
      mockRequest.delete.mockResolvedValue({ message: '删除成功' })

      const result = await deletePost(1)

      expect(result.message).toBe('删除成功')
      expect(mockRequest.delete).toHaveBeenCalledWith('/posts/1', { loadingText: '删除中...' })
    })
  })

  describe('toggleLikePost()', () => {
    it('should like a post', async () => {
      mockRequest.post.mockResolvedValue({
        liked: true,
        likeCount: 11
      })

      const result = await toggleLikePost(1)

      expect(result.liked).toBe(true)
      expect(result.likeCount).toBe(11)
      expect(mockRequest.post).toHaveBeenCalledWith('/posts/1/like', null, { showSuccess: false, showError: false })
    })

    it('should unlike a post (toggle)', async () => {
      mockRequest.post.mockResolvedValue({
        liked: false,
        likeCount: 10
      })

      const result = await toggleLikePost(1)

      expect(result.liked).toBe(false)
      expect(result.likeCount).toBe(10)
    })
  })

  describe('checkPostLiked()', () => {
    it('should check if post is liked', async () => {
      mockRequest.get.mockResolvedValue({ liked: true })

      const result = await checkPostLiked(1)

      expect(result.liked).toBe(true)
      expect(mockRequest.get).toHaveBeenCalledWith('/posts/1/like/check', { showLoading: false })
    })
  })

  describe('toggleCollectPost()', () => {
    it('should collect a post', async () => {
      mockRequest.post.mockResolvedValue({
        collected: true,
        collectCount: 6
      })

      const result = await toggleCollectPost(1)

      expect(result.collected).toBe(true)
      expect(result.collectCount).toBe(6)
    })

    it('should uncollect a post (toggle)', async () => {
      mockRequest.post.mockResolvedValue({
        collected: false,
        collectCount: 5
      })

      const result = await toggleCollectPost(1)

      expect(result.collected).toBe(false)
      expect(result.collectCount).toBe(5)
    })
  })

  describe('checkPostCollected()', () => {
    it('should check if post is collected', async () => {
      mockRequest.get.mockResolvedValue({ collected: true })

      const result = await checkPostCollected(1)

      expect(result.collected).toBe(true)
      expect(mockRequest.get).toHaveBeenCalledWith('/posts/1/collect/check', { showLoading: false })
    })
  })
})
