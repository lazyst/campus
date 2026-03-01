import { test, expect } from '@playwright/test'
import { adminLogin } from './admin-login.spec'

test.describe('仪表盘数据', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page)
  })

  test('应该正确显示统计数据', async ({ page }) => {
    // 等待仪表盘页面加载
    await page.waitForURL('/')

    // 验证页面标题
    await expect(page.locator('.header-title')).toContainText('数据概览')

    // 等待统计数据卡片加载
    await page.waitForSelector('.stat-card', { timeout: 10000 })

    // 验证统计数据卡片存在（用户数、帖子数、商品数等）
    const statCards = page.locator('.stat-card')
    await expect(statCards).toHaveCount(4) // 应该有4个统计卡片

    // 验证统计卡片包含数据
    const firstStatCard = statCards.first()
    await expect(firstStatCard).toBeVisible()
  })

  test('应该显示趋势图表', async ({ page }) => {
    // 等待图表加载
    await page.waitForSelector('.trend-chart, .echart, canvas', { timeout: 10000 })

    // 验证图表可见
    const chart = page.locator('canvas').first()
    await expect(chart).toBeVisible()
  })

  test('应该显示最近活动', async ({ page }) => {
    // 等待最近活动组件加载
    await page.waitForSelector('.recent-activity, .el-table', { timeout: 10000 })

    // 验证最近活动区域可见
    const activitySection = page.locator('.recent-activity, .el-card').first()
    await expect(activitySection).toBeVisible()
  })
})
