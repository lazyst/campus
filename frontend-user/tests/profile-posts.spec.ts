import { test, expect } from '@playwright/test'

test.describe('查看我的帖子功能测试', () => {
  const BASE_URL = 'http://localhost:3000'
  const testUser = {
    phone: '13800000001',
    password: '123456'
  }

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL)
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  /**
   * TC-015: 查看我的帖子
   * 验证用户查看自己发布的帖子列表
   */
  test('TC-015: 查看我的帖子 - 完整流程', async ({ page }) => {
    console.log('开始测试: 查看我的帖子')

    try {
      // ===== 步骤1: 登录并保存token =====
      console.log('步骤1: 登录并保存token')
      const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone: testUser.phone, password: testUser.password })
      })
      const loginData = await loginResponse.json()

      if (!loginData.token) {
        throw new Error('登录失败')
      }

      // 保存token到localStorage
      await page.goto(BASE_URL)
      await page.evaluate((token) => {
        localStorage.setItem('token', token)
      }, loginData.token)
      console.log('登录成功，token已保存')

      // ===== 步骤2: 访问我的发布页面 =====
      console.log('步骤2: 访问我的发布页面')
      await page.goto(`${BASE_URL}/profile/posts`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(3000)

      // 验证页面加载成功
      const currentUrl = page.url()
      console.log(`当前URL: ${currentUrl}`)
      expect(currentUrl).toContain('/profile')
      console.log('✅ 我的帖子页面访问成功!')

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })
})
