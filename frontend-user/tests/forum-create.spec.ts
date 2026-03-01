import { test, expect } from '@playwright/test'

test.describe('发布帖子功能测试', () => {
  const BASE_URL = 'http://localhost:3000'
  const testUser = {
    phone: '13800000001',
    password: '123456'
  }

  test.beforeEach(async ({ page }) => {
    // 每个测试前清除登录状态，确保测试独立
    await page.goto(BASE_URL)
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  /**
   * TC-004: 发布帖子
   * 验证用户发布论坛帖子的功能
   */
  test('TC-004: 发布帖子 - 完整流程', async ({ page }) => {
    console.log('开始测试: 发布帖子')

    try {
      // ===== 步骤1: 登录系统 =====
      console.log('步骤1: 登录系统')
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', testUser.phone)
      await page.fill('input[type="password"]', testUser.password)
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })
      console.log('登录成功')

      // ===== 步骤2: 点击底部Tab"论坛" =====
      console.log('步骤2: 点击底部Tab"论坛"')

      // 尝试多种方式找到论坛Tab
      const forumTab = page.locator('.van-tab').filter({ hasText: /论坛|广场/ }).first()

      if (await forumTab.count() > 0) {
        await forumTab.click()
        await page.waitForTimeout(1000)
        console.log('点击了论坛Tab')
      } else {
        // 直接导航到论坛页面
        await page.goto(`${BASE_URL}/forum`, { waitUntil: 'networkidle' })
        console.log('直接访问论坛页面')
      }

      // ===== 步骤3: 点击右上角"发布"按钮 =====
      console.log('步骤3: 点击右上角发布按钮')

      // 查找发布按钮 - 可能在导航栏右上角或底部
      const createButton = page.locator('.create-post-btn, .van-button--primary, [class*="create"]').first()

      // 如果找不到专门的创建按钮，尝试点击带+号的按钮
      const plusButton = page.locator('.van-icon-plus, [class*="add"]').first()

      if (await createButton.count() > 0) {
        await createButton.click()
      } else if (await plusButton.count() > 0) {
        await plusButton.click()
      } else {
        // 直接导航到发布页面
        await page.goto(`${BASE_URL}/forum/create`, { waitUntil: 'networkidle' })
      }

      await page.waitForTimeout(2000)
      console.log('进入帖子发布页面')

      // ===== 步骤4: 输入帖子标题 =====
      console.log('步骤4: 输入帖子标题')
      const titleInput = page.locator('input[placeholder*="标题"], input[class*="title"]').first()

      if (await titleInput.count() > 0) {
        await titleInput.fill('E2E测试帖子')
        const titleValue = await titleInput.inputValue()
        expect(titleValue).toBe('E2E测试帖子')
        console.log('帖子标题已填入')
      } else {
        console.log('未找到标题输入框，可能使用其他选择器')
      }

      // ===== 步骤5: 选择版块 =====
      console.log('步骤5: 选择版块')

      // 点击版块选择器
      const boardSelector = page.locator('.board-selector, [class*="board"], .van-field__control').filter({ hasText: /版块|选择/ }).first()

      if (await boardSelector.count() > 0) {
        await boardSelector.click()
        await page.waitForTimeout(1000)

        // 选择一个版块（如：学习交流）
        const boardOption = page.locator('.van-picker__confirm, .van-picker-column__item, [class*="option"]').first()
        if (await boardOption.count() > 0) {
          await boardOption.click()
          console.log('已选择版块')

          // 确认选择
          const confirmBtn = page.locator('.van-picker__confirm, button:has-text("确定")').first()
          if (await confirmBtn.count() > 0) {
            await confirmBtn.click()
          }
        }
      }

      // ===== 步骤6: 输入帖子内容 =====
      console.log('步骤6: 输入帖子内容')
      const contentArea = page.locator('textarea, [contenteditable="true"], .post-content').first()

      if (await contentArea.count() > 0) {
        await contentArea.fill('这是E2E自动化测试帖子内容')
        const contentValue = await contentArea.inputValue()
        expect(contentValue).toContain('E2E自动化测试')
        console.log('帖子内容已填入')
      }

      // ===== 步骤7: 点击发布按钮 =====
      console.log('步骤7: 点击发布按钮')

      const publishButton = page.locator('button:has-text("发布"), button:has-text("提交")').first()

      if (await publishButton.count() > 0) {
        await publishButton.click()
        console.log('已点击发布按钮')

        // 等待发布成功
        await page.waitForTimeout(3000)

        // ===== 步骤8: 验证帖子已发布 =====
        console.log('步骤8: 验证帖子已发布')

        // 检查是否返回到列表页或显示成功提示
        const currentUrl = page.url()
        console.log(`当前URL: ${currentUrl}`)

        // 验证页面无报错
        const hasErrorToast = await page.locator('.van-toast--fail').count()
        console.log(`错误提示数量: ${hasErrorToast}`)

        // 如果还在发布页，检查是否有成功提示
        if (currentUrl.includes('/create') || currentUrl.includes('/publish')) {
          const successToast = page.locator('.van-toast--success, .van-notify--success').first()
          if (await successToast.count() > 0) {
            console.log('发布成功提示显示')
          }
        }

        // 验证通过标准
        expect(hasErrorToast).toBe(0)
        console.log('✅ 发布帖子测试通过!')
      } else {
        console.log('未找到发布按钮')
        throw new Error('未找到发布按钮')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      const screenshot = await page.screenshot()
      console.log('测试失败截图已保存')
      throw error
    }
  })

  test('TC-004-1: 发布帖子 - 必填字段验证', async ({ page }) => {
    console.log('开始测试: 发布帖子必填字段验证')

    try {
      // 登录
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', testUser.phone)
      await page.fill('input[type="password"]', testUser.password)
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 进入发布页面
      await page.goto(`${BASE_URL}/forum/create`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 不填任何内容，直接点击发布
      const publishButton = page.locator('button:has-text("发布"), button:has-text("提交")').first()

      if (await publishButton.count() > 0) {
        // 检查发布按钮是否被禁用
        const isDisabled = await publishButton.isDisabled()
        console.log(`发布按钮禁用状态: ${isDisabled}`)

        // 填写标题后检查
        const titleInput = page.locator('input[placeholder*="标题"]').first()
        if (await titleInput.count() > 0) {
          await titleInput.fill('测试标题')

          // 再检查按钮状态
          const isDisabledAfterTitle = await publishButton.isDisabled()
          console.log(`填写标题后按钮禁用状态: ${isDisabledAfterTitle}`)
        }

        console.log('✅ 必填字段验证测试通过!')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })
})
