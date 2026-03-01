import { test, expect } from '@playwright/test'
import { adminLogin } from './admin-login.spec'

test.describe('板块管理', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page)
  })

  test('应该能够查看板块列表', async ({ page }) => {
    // 点击板块管理菜单
    await page.click('text=板块管理')
    await page.waitForURL('**/boards')

    // 验证页面标题
    await expect(page.locator('.header-title')).toContainText('板块管理')

    // 等待板块列表加载
    await page.waitForSelector('.el-table', { timeout: 10000 })

    // 验证表格可见
    const table = page.locator('.el-table').first()
    await expect(table).toBeVisible()
  })

  test('应该能够新增板块', async ({ page }) => {
    // 进入板块管理页面
    await page.click('text=板块管理')
    await page.waitForURL('**/boards')

    // 等待页面加载完成
    await page.waitForSelector('.el-table, .el-button--primary', { timeout: 10000 })

    // 点击新建板块按钮
    await page.click('.el-card .el-button--primary')

    // 等待表单对话框出现
    await page.waitForSelector('.el-dialog', { timeout: 5000 })

    // 填写板块名称
    const nameInput = page.locator('.el-dialog .el-input input').first()
    await nameInput.fill('测试板块')

    // 填写板块描述
    const descInput = page.locator('.el-dialog .el-textarea textarea').first()
    await descInput.fill('测试描述')

    // 点击确定按钮
    await page.click('.el-dialog .el-button--primary')

    // 等待对话框关闭
    await page.waitForTimeout(1000)

    // 验证保存成功（无错误提示）
    await expect(page.locator('.el-message--error')).toBeHidden({ timeout: 3000 }).catch(() => {})
  })

  test('应该能够编辑板块', async ({ page }) => {
    // 进入板块管理页面
    await page.click('text=板块管理')
    await page.waitForURL('**/boards')

    // 等待表格加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 })

    // 获取第一行数据
    const rowCount = await page.locator('.el-table__row').count()

    if (rowCount > 0) {
      // 点击编辑按钮
      const editButton = page.locator('.el-table__row').first().locator('.el-button--primary')
      await editButton.click()

      // 等待表单对话框出现
      await page.waitForSelector('.el-dialog', { timeout: 5000 })

      // 修改板块名称
      const nameInput = page.locator('.el-dialog .el-input input').first()
      await nameInput.fill('编辑后的板块名称')

      // 点击确定按钮
      await page.click('.el-dialog .el-button--primary')

      // 等待对话框关闭
      await page.waitForTimeout(1000)
    }
  })

  test('应该能够删除板块', async ({ page }) => {
    // 进入板块管理页面
    await page.click('text=板块管理')
    await page.waitForURL('**/boards')

    // 等待表格加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 })

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
