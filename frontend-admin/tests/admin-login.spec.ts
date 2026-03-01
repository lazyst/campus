import { test, expect } from '@playwright/test'

/**
 * 管理员登录 fixture
 */
export async function adminLogin(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="用户名"]', 'admin')
  await page.fill('input[placeholder="密码"]', 'admin123')
  await page.click('button.login-btn')
  await page.waitForURL('/')
}

test.describe('管理员登录', () => {
  test('应该能够成功登录管理后台', async ({ page }) => {
    // 访问登录页
    await page.goto('/login')

    // 验证登录页加载
    await expect(page.locator('h2')).toContainText('校园互助平台')
    await expect(page.locator('.subtitle')).toContainText('管理后台')

    // 输入用户名和密码
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'admin123')

    // 点击登录按钮
    await page.click('button.login-btn')

    // 等待页面跳转并验证
    await page.waitForURL('/')

    // 验证进入仪表盘
    await expect(page.locator('.header-title')).toContainText('数据概览')

    // 验证侧边栏显示
    await expect(page.locator('.sidebar .logo-text')).toContainText('校园互助')

    // 验证管理员头像显示
    await expect(page.locator('.user-avatar')).toContainText('管')
    await expect(page.locator('.user-name')).toContainText('管理员')
  })

  test('用户名或密码错误应该显示错误提示', async ({ page }) => {
    await page.goto('/login')

    // 输入错误的用户名和密码
    await page.fill('input[placeholder="用户名"]', 'admin')
    await page.fill('input[placeholder="密码"]', 'wrongpassword')

    // 点击登录按钮
    await page.click('button.login-btn')

    // 验证错误提示
    await expect(page.locator('.el-message')).toContainText('登录失败')
  })
})
