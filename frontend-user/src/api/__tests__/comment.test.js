import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getCommentsByPost, createComment, deleteComment } from '../modules/comment'

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

describe('Comment API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getCommentsByPost()', () => {
    it('should get comments for a post', async () => {
      const mockComments = {
        records: [
          { id: 1, postId: 1, content: '评论1', userId: 2 },
          { id: 2, postId: 1, content: '评论2', userId: 3 }
        ],
        total: 2
      }

      mockRequest.get.mockResolvedValue(mockComments)

      const result = await getCommentsByPost(1)

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/comments/post/1', { showLoading: false })
    })
  })

  describe('createComment()', () => {
    it('should create a comment successfully', async () => {
      const newComment = {
        postId: 1,
        content: '这是新评论'
      }

      const createdComment = {
        id: 1,
        ...newComment,
        userId: 2,
        createdAt: '2026-01-27T10:00:00'
      }

      mockRequest.post.mockResolvedValue(createdComment)

      const result = await createComment(newComment)

      expect(result.content).toBe('这是新评论')
      expect(mockRequest.post).toHaveBeenCalledWith('/comments', newComment, { loadingText: '发表中...' })
    })
  })

  describe('deleteComment()', () => {
    it('should delete a comment successfully', async () => {
      mockRequest.delete.mockResolvedValue({ message: '删除成功' })

      const result = await deleteComment(1)

      expect(result.message).toBe('删除成功')
      expect(mockRequest.delete).toHaveBeenCalledWith('/comments/1', { loadingText: '删除中...' })
    })
  })
})
