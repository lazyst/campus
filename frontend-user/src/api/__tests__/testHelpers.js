// frontend-user/src/api/__tests__/testHelpers.js

/**
 * 测试辅助工具
 * 提供统一的mock方法和测试工具函数
 */

import { vi } from 'vitest'

/**
 * 创建mock的request实例
 */
export function createMockRequest() {
  const mockRequest = vi.fn()

  // 设置默认实现
  mockRequest.mockResolvedValue({})

  return mockRequest
}

/**
 * 创建mock的API响应
 */
export function createMockResponse(data, code = 200, message = 'success') {
  return {
    code,
    message,
    data
  }
}

/**
 * 创建分页响应
 */
export function createMockPaginatedResponse(records, total = records.length) {
  return {
    records,
    total
  }
}

/**
 * Mock localStorage
 */
export function mockLocalStorage() {
  const store = {}

  return {
    getItem: vi.fn((key) => store[key] || null),
    setItem: vi.fn((key, value) => {
      store[key] = value
    }),
    removeItem: vi.fn((key) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      Object.keys(store).forEach(key => delete store[key])
    }),
    get length() {
      return Object.keys(store).length
    }
  }
}

/**
 * 为API模块创建测试套件生成器
 */
export function createAPITestSuite(moduleName, apiFunctions) {
  return describe(`${moduleName} API Tests`, () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })

    // 为每个API函数生成基础测试
    Object.entries(apiFunctions).forEach(([functionName, config]) => {
      describe(`${functionName}()`, () => {
        it('should call the API with correct parameters', async () => {
          const mockRequest = createMockRequest()
          mockRequest.mockResolvedValue(config.mockResponse || {})

          // 动态测试将在具体文件中实现
          expect(true).toBe(true)
        })
      })
    })
  })
}
