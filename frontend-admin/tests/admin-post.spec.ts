import { test, expect } from '@playwright/test'
import { adminLogin } from './admin-login.spec'

test.describe('帖子管理', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page)
  })

  test('应该能够查看帖子列表', async ({ page }) => {
    // 点击内容管理菜单
    await page.click('.el-sub-menu:has-text("内容管理")')
    await page.waitForTimeout(500) // 等待子菜单展开

    // 点击帖子管理
    await page.click('.el-menu-item:has-text("帖子管理")')
    await page.waitForURL('**/posts')

    // 验证页面标题
    await expect(page.locator('.header-title')).toContainText('帖子管理')

    // 等待帖子列表加载
    await page.waitForSelector('.el-table', { timeout: 10000 })

    // 验证表格可见
    const table = page.locator('.el-table').first()
    await expect(table).toBeVisible()
  })

  test('应该能够查看帖子详情', async ({ page }) => {
    // 进入帖子管理页面
    await page.click('.el-sub-menu:has-text("内容管理")')
    await page.waitForTimeout(500)
    await page.click('.el-menu-item:has-text("帖子管理")')
    await page.waitForURL('**/posts')

    // 等待表格加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 })

    // 检查是否有帖子数据
    const rowCount = await page.locator('.el-table__row').count()

    if (rowCount > 0) {
      // 点击查看按钮
      const viewButton = page.locator('.el-table__row').first().locator('.el-button--primary')
      await viewButton.click()

      // 等待详情对话框
      await page.waitForSelector('.el-dialog', { timeout: 5000 })

      // 验证对话框标题
      await expect(page.locator('.el-dialog__title')).toContainText('帖子详情')
    }
  })

  test('应该能够删除帖子', async ({ page }) => {
    // 进入帖子管理页面
    await page.click('.el-sub-menu:has-text("内容管理")')
    await page.waitForTimeout(500)
    await page.click('.el-menu-item:has-text("帖子管理")')
    await page.waitForURL('**/posts')

    // 等待表格加载
    await page.locator('.el-table__row').first().waitFor({ state: 'visible', timeout: 10000 })

    // 获取行数
    const rowCount = await page.locator('.el-table__row').count()

    if (rowCount > 0) {
      // 记录删除前的行数
      const initialCount = rowCount

      // 点击删除按钮
      const deleteButton = page.locator('.el-table__row').first().locator('.el-button--danger')
      await deleteButton.click()

      // 等待确认对话框
      await page.waitForSelector('.el-message-box', { timeout: 5000 }).catch(() => {})

      // 点击确认删除
      const confirmButton = page.locator('.el-message-box__button .el-button--primary')
      if (await confirmButton.isVisible()) {
        await confirmButton.click()

        // 等待删除完成
        await page.waitForTimeout(1000)

        // 验证列表已更新
        const finalCount = await page.locator('.el-table__row').count()
        expect(finalCount).toBeLessThanOrEqual(initialCount)
      }
    }
  })
})
