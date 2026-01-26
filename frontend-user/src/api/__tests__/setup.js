// frontend-user/src/api/__tests__/setup.js

import { vi } from 'vitest'

// Mock localStorage
global.localStorage = {
  getItem: vi.fn(() => null),
  setItem: vi.fn(() => {}),
  removeItem: vi.fn(() => {}),
  clear: vi.fn(() => {})
}

// Mock axios before any imports
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      }
    }))
  }
}))

// Mock request module
vi.mock('../request', () => ({
  default: vi.fn(() => Promise.resolve({}))
}))
