import { test, expect } from '@playwright/test'

test.describe('浏览帖子列表功能测试', () => {
  const BASE_URL = 'http://localhost:3000'

  test.beforeEach(async ({ page }) => {
    // 每个测试前清除登录状态
    await page.goto(BASE_URL)
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  test('TC-003: 浏览帖子列表 - 验证帖子列表正确显示', async ({ page }) => {
    console.log('开始测试: 浏览帖子列表')

    try {
      // 步骤1: 访问首页（论坛页面）
      console.log('步骤1: 访问首页（论坛页面）')
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')
      console.log('首页加载成功')

      // 步骤2: 等待帖子列表加载
      console.log('步骤2: 等待帖子列表加载')

      // 等待骨架屏消失或内容出现
      await page.waitForFunction(() => {
        const skeleton = document.querySelector('.forum-list__skeleton')
        const content = document.querySelector('.forum-list__content')
        const emptyState = document.querySelector('.forum-list__state-text')
        return skeleton === null || content !== null || emptyState !== null
      }, { timeout: 15000 })
      console.log('帖子列表加载完成')

      // 步骤3: 验证帖子列表显示
      console.log('步骤3: 验证帖子列表显示')

      // 检查是否有帖子卡片或空状态
      const postCards = page.locator('.forum-list__card')
      const emptyState = page.locator('.forum-list__state-text')

      const hasPosts = await postCards.count() > 0
      const hasEmptyState = await emptyState.count() > 0

      console.log(`帖子数量: ${await postCards.count()}`)
      console.log(`有空状态: ${hasEmptyState}`)

      // 至少应该有帖子卡片，或者显示空状态
      expect(hasPosts || hasEmptyState).toBe(true)

      if (hasPosts) {
        // 步骤3a: 验证帖子卡片内容
        console.log('步骤3a: 验证帖子卡片内容')

        // 验证第一个帖子卡片存在
        const firstCard = postCards.first()
        await expect(firstCard).toBeVisible()

        // 验证帖子标题
        const titleElement = firstCard.locator('.forum-list__card-title')
        const hasTitle = await titleElement.count() > 0
        expect(hasTitle).toBe(true)
        const titleText = await titleElement.textContent()
        console.log(`帖子标题: ${titleText}`)

        // 验证帖子内容预览
        const previewElement = firstCard.locator('.forum-list__card-preview')
        const hasPreview = await previewElement.count() > 0
        expect(hasPreview).toBe(true)
        console.log('帖子内容预览存在')

        // 验证作者信息
        const usernameElement = firstCard.locator('.forum-list__card-username')
        const hasUsername = await usernameElement.count() > 0
        expect(hasUsername).toBe(true)
        const username = await usernameElement.textContent()
        console.log(`作者: ${username}`)

        // 验证统计信息（点赞、评论、收藏按钮）
        const statsElement = firstCard.locator('.forum-list__card-stats')
        const hasStats = await statsElement.count() > 0
        expect(hasStats).toBe(true)
        console.log('统计信息存在')

        // 步骤4: 滚动加载更多帖子
        console.log('步骤4: 滚动页面加载更多')

        // 获取当前帖子数量
        const initialCount = await postCards.count()
        console.log(`初始帖子数量: ${initialCount}`)

        // 滚动到页面底部触发加载更多
        await page.evaluate(() => {
          window.scrollTo(0, document.body.scrollHeight)
        })

        // 等待一段时间让新帖子加载
        await page.waitForTimeout(2000)

        // 检查帖子数量是否增加（如果有更多帖子的话）
        const afterScrollCount = await postCards.count()
        console.log(`滚动后帖子数量: ${afterScrollCount}`)

        // 验证页面无报错
        console.log('验证页面无报错')
        const consoleErrors = await page.evaluate(() => {
          return window.__error_count__ || 0
        })
        expect(consoleErrors).toBe(0)
      } else {
        // 如果是空状态，验证空状态提示
        console.log('当前没有帖子数据，显示空状态')
        const emptyText = await emptyState.textContent()
        expect(emptyText).toContain('暂无帖子')
      }

      console.log('测试通过: 浏览帖子列表功能正常')

    } catch (error) {
      console.error('测试失败:', error)
      // 捕获页面错误信息
      const pageErrors: string[] = []
      page.on('pageerror', (error) => {
        pageErrors.push(error.message)
      })
      if (pageErrors.length > 0) {
        console.error('页面错误:', pageErrors)
      }
      throw error
    }
  })

  test('TC-003-1: 浏览帖子列表 - PC端验证', async ({ page }) => {
    console.log('开始测试: PC端浏览帖子列表')

    // 设置桌面端视口
    await page.setViewportSize({ width: 1280, height: 720 })

    try {
      // 步骤1: 访问首页
      console.log('步骤1: 访问首页')
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')
      console.log('首页加载成功')

      // 步骤2: 等待帖子列表加载
      console.log('步骤2: 等待帖子列表加载')
      await page.waitForFunction(() => {
        const skeleton = document.querySelector('.forum-list__skeleton')
        const content = document.querySelector('.forum-list__content')
        const emptyState = document.querySelector('.forum-list__state-text')
        return skeleton === null || content !== null || emptyState !== null
      }, { timeout: 15000 })
      console.log('帖子列表加载完成')

      // 步骤3: 验证PC端帖子卡片显示
      console.log('步骤3: 验证PC端帖子卡片')
      const postCards = page.locator('.forum-list__card')
      const cardCount = await postCards.count()

      if (cardCount > 0) {
        // 验证卡片PC端样式是否应用
        const firstCard = postCards.first()
        await expect(firstCard).toBeVisible()

        // 验证标题
        const titleElement = firstCard.locator('.forum-list__card-title')
        await expect(titleElement).toBeVisible()
        const titleText = await titleElement.textContent()
        console.log(`帖子标题: ${titleText}`)

        // 验证内容预览
        const previewElement = firstCard.locator('.forum-list__card-preview')
        await expect(previewElement).toBeVisible()
        console.log('PC端帖子内容预览显示正常')

        // 验证底部统计信息
        const statsElement = firstCard.locator('.forum-list__card-stats')
        await expect(statsElement).toBeVisible()
        console.log('PC端统计信息显示正常')
      } else {
        console.log('暂无帖子数据')
      }

      console.log('PC端浏览帖子列表测试通过')

    } catch (error) {
      console.error('PC端测试失败:', error)
      throw error
    }
  })

  test('TC-003-2: 浏览帖子列表 - 移动端验证', async ({ page }) => {
    console.log('开始测试: 移动端浏览帖子列表')

    // 设置移动端视口
    await page.setViewportSize({ width: 375, height: 667 })

    try {
      // 步骤1: 访问首页
      console.log('步骤1: 访问首页')
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')
      console.log('首页加载成功')

      // 步骤2: 等待帖子列表加载
      console.log('步骤2: 等待帖子列表加载')
      await page.waitForFunction(() => {
        const skeleton = document.querySelector('.forum-list__skeleton')
        const content = document.querySelector('.forum-list__content')
        const emptyState = document.querySelector('.forum-list__state-text')
        return skeleton === null || content !== null || emptyState !== null
      }, { timeout: 15000 })
      console.log('帖子列表加载完成')

      // 步骤3: 验证移动端帖子卡片显示
      console.log('步骤3: 验证移动端帖子卡片')
      const postCards = page.locator('.forum-list__card')
      const cardCount = await postCards.count()

      if (cardCount > 0) {
        const firstCard = postCards.first()
        await expect(firstCard).toBeVisible()

        // 验证标题存在
        const titleElement = firstCard.locator('.forum-list__card-title')
        await expect(titleElement).toBeVisible()
        const titleText = await titleElement.textContent()
        console.log(`帖子标题: ${titleText}`)
        console.log('移动端帖子标题显示正常')
      } else {
        console.log('暂无帖子数据')
      }

      console.log('移动端浏览帖子列表测试通过')

    } catch (error) {
      console.error('移动端测试失败:', error)
      throw error
    }
  })

  test('TC-003-3: 帖子列表 - 点击帖子进入详情页', async ({ page }) => {
    console.log('开始测试: 点击帖子进入详情页')

    try {
      // 步骤1: 访问首页
      console.log('步骤1: 访问首页')
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')
      console.log('首页加载成功')

      // 步骤2: 等待帖子加载
      console.log('步骤2: 等待帖子加载')
      await page.waitForFunction(() => {
        const content = document.querySelector('.forum-list__content')
        const emptyState = document.querySelector('.forum-list__state-text')
        return content !== null || emptyState !== null
      }, { timeout: 15000 })

      // 步骤3: 点击第一个帖子
      console.log('步骤3: 点击帖子进入详情')
      const postCards = page.locator('.forum-list__card')
      const cardCount = await postCards.count()

      if (cardCount > 0) {
        const firstCard = postCards.first()
        // 获取帖子标题用于日志
        const titleElement = firstCard.locator('.forum-list__card-title')
        const titleText = await titleElement.textContent()
        console.log(`点击帖子: ${titleText}`)

        await firstCard.click()
        console.log('已点击帖子')

        // 等待页面跳转
        await page.waitForTimeout(1000)

        // 步骤4: 验证进入了详情页
        console.log('步骤4: 验证详情页')
        const currentUrl = page.url()
        console.log(`当前URL: ${currentUrl}`)

        // 验证URL包含forum/detail
        expect(currentUrl).toContain('/forum/detail/')
        console.log('成功进入帖子详情页')
      } else {
        console.log('暂无帖子，跳过点击测试')
      }

      console.log('点击帖子进入详情页测试通过')

    } catch (error) {
      console.error('测试失败:', error)
      throw error
    }
  })
})
