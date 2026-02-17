// frontend-user/src/api/__tests__/comment.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getCommentsByPost, createComment, deleteComment } from '../modules/comment'

const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn()
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

      mockRequest.mockResolvedValue(mockComments)

      const result = await getCommentsByPost(1, { page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/posts/1/comments',
        method: 'get',
        params: { page: 1, size: 10 }
      })
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

      mockRequest.mockResolvedValue(createdComment)

      const result = await createComment(newComment)

      expect(result.content).toBe('这是新评论')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/comments',
        method: 'post',
        data: newComment
      })
    })
  })

  describe('deleteComment()', () => {
    it('should delete a comment successfully', async () => {
      mockRequest.mockResolvedValue({ message: '删除成功' })

      const result = await deleteComment(1)

      expect(result.message).toBe('删除成功')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/comments/1',
        method: 'delete'
      })
    })
  })
})
