import { test, expect } from '@playwright/test'

test.describe('测试', () => {
  test('基本测试', async ({ page }) => {
    await page.goto('http://localhost:3001/admin')
    await page.waitForLoadState('domcontentloaded')
    console.log('页面加载成功')
  })
})
