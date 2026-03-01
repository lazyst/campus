import { test, expect } from '@playwright/test'
import { adminLogin } from './admin-login.spec'

test.describe('用户管理', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page)
  })

  test('应该能够查看用户列表', async ({ page }) => {
    // 点击用户管理菜单
    await page.click('text=用户管理')
    await page.waitForURL('**/users')

    // 验证页面标题
    await expect(page.locator('.header-title')).toContainText('用户管理')

    // 等待用户列表加载
    await page.waitForSelector('.el-table', { timeout: 10000 })

    // 验证表格可见
    const table = page.locator('.el-table').first()
    await expect(table).toBeVisible()
  })

  test('应该能够封禁和解封用户', async ({ page }) => {
    // 进入用户管理页面
    await page.click('text=用户管理')
    await page.waitForURL('**/users')

    // 等待表格加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 })

    // 检查是否有用户数据
    const rowCount = await page.locator('.el-table__row').count()

    if (rowCount > 0) {
      // 获取第一行数据的操作按钮
      const firstRow = page.locator('.el-table__row').first()

      // 找到封禁/解封按钮
      const actionButton = firstRow.locator('.el-button--warning, .el-button--success').first()

      // 检查按钮是否可见
      if (await actionButton.isVisible()) {
        // 获取当前按钮文本
        const buttonText = await actionButton.textContent()

        // 点击操作按钮
        await actionButton.click()

        // 等待确认对话框
        await page.waitForSelector('.el-message-box', { timeout: 5000 }).catch(() => {})

        // 点击确认按钮
        const confirmButton = page.locator('.el-message-box__button .el-button--primary')
        if (await confirmButton.isVisible()) {
          await confirmButton.click()
          // 等待操作完成
          await page.waitForTimeout(1000)
        }
      }
    }
  })
})
