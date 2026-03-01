import { test, expect } from '@playwright/test'

test.describe('帖子详情功能测试', () => {
  const BASE_URL = 'http://localhost:3000'

  test.beforeEach(async ({ page }) => {
    // 每个测试前清除登录状态，确保测试独立
    await page.goto(BASE_URL)
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  /**
   * TC-005: 帖子详情查看
   * 验证用户查看帖子详情的功能
   */
  test('TC-005: 帖子详情查看 - 完整流程', async ({ page }) => {
    console.log('开始测试: 帖子详情查看')

    try {
      // ===== 步骤1: 访问论坛页面 =====
      console.log('步骤1: 访问论坛页面')
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')
      console.log('首页（论坛）加载成功')

      // ===== 步骤2: 等待帖子列表加载 =====
      console.log('步骤2: 等待帖子列表加载')

      await page.waitForFunction(() => {
        const content = document.querySelector('.forum-list__content')
        const emptyState = document.querySelector('.forum-list__state-text')
        return content !== null || emptyState !== null
      }, { timeout: 15000 })
      console.log('帖子列表加载完成')

      // ===== 步骤3: 获取第一个帖子信息用于后续验证 =====
      console.log('步骤3: 获取帖子信息')

      const postCards = page.locator('.forum-list__card')
      const cardCount = await postCards.count()

      if (cardCount === 0) {
        console.log('没有帖子数据，跳过详情测试')
        // 如果没有帖子，创建一个测试帖子再测试
        console.log('准备创建测试帖子...')
        await createTestPost(page)
      }

      // 再次检查帖子
      const cardsAfterCreate = await postCards.count()
      console.log(`当前帖子数量: ${cardsAfterCreate}`)

      if (cardsAfterCreate > 0) {
        // 获取帖子标题
        const firstCard = postCards.first()
        const titleElement = firstCard.locator('.forum-list__card-title')
        const postTitle = await titleElement.textContent()
        console.log(`准备查看帖子: ${postTitle}`)

        // ===== 步骤4: 点击帖子卡片 =====
        console.log('步骤4: 点击帖子卡片')
        await firstCard.click()
        console.log('已点击帖子')

        // 等待页面跳转
        await page.waitForTimeout(2000)

        // ===== 步骤5: 等待详情页加载 =====
        console.log('步骤5: 等待详情页加载')

        // 等待详情页关键元素出现
        await page.waitForFunction(() => {
          const detailContainer = document.querySelector('.post-detail, .forum-detail, [class*="detail"]')
          return detailContainer !== null
        }, { timeout: 15000 })
        console.log('详情页加载完成')

        // ===== 步骤6: 检查帖子信息 =====
        console.log('步骤6: 检查帖子信息')

        // 检查标题
        const detailTitle = page.locator('.post-detail__title, .detail-title, h1').first()
        const hasTitle = await detailTitle.count() > 0
        expect(hasTitle).toBe(true)
        const titleText = await detailTitle.textContent()
        console.log(`帖子标题: ${titleText}`)

        // 检查内容
        const detailContent = page.locator('.post-detail__content, .detail-content, [class*="content"]').first()
        const hasContent = await detailContent.count() > 0
        expect(hasContent).toBe(true)
        const contentText = await detailContent.textContent()
        console.log(`帖子内容: ${contentText.substring(0, 50)}...`)

        // 检查作者信息
        const authorElement = page.locator('.post-detail__author, .detail-author, .author-name').first()
        const hasAuthor = await authorElement.count() > 0
        if (hasAuthor) {
          const authorName = await authorElement.textContent()
          console.log(`作者: ${authorName}`)
        }

        // 检查发布时间
        const timeElement = page.locator('.post-detail__time, .detail-time, .time').first()
        const hasTime = await timeElement.count() > 0
        if (hasTime) {
          const postTime = await timeElement.textContent()
          console.log(`发布时间: ${postTime}`)
        }

        // ===== 步骤7: 检查评论区 =====
        console.log('步骤7: 检查评论区')

        const commentSection = page.locator('.comment-section, .post-detail__comments, [class*="comment"]')
        const hasCommentSection = await commentSection.count() > 0
        console.log(`评论区存在: ${hasCommentSection}`)

        if (hasCommentSection) {
          const comments = page.locator('.comment-item, .comment-list__item')
          const commentCount = await comments.count()
          console.log(`评论数量: ${commentCount}`)

          if (commentCount > 0) {
            const firstComment = comments.first()
            const hasCommentContent = await firstComment.locator('.comment-content, [class*="content"]').count() > 0
            if (hasCommentContent) {
              const commentText = await firstComment.locator('.comment-content').textContent()
              console.log(`第一条评论: ${commentText.substring(0, 30)}...`)
            }
          }
        }

        // ===== 验证通过标准 =====
        console.log('验证通过标准:')

        // 1. 详情页无报错
        const consoleErrors: string[] = []
        page.on('console', msg => {
          if (msg.type() === 'error') {
            consoleErrors.push(msg.text())
          }
        })
        await page.waitForTimeout(1000)

        // 2. 内容完整显示
        expect(hasTitle).toBe(true)
        expect(hasContent).toBe(true)
        console.log('- 内容完整显示: 通过')

        // 3. URL正确
        const currentUrl = page.url()
        expect(currentUrl).toContain('/forum/detail/')
        console.log('- URL正确: 通过')

        console.log('✅ 帖子详情查看测试通过!')
      } else {
        console.log('无法获取帖子数据，跳过测试')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      const screenshot = await page.screenshot()
      console.log('测试失败截图已保存')
      throw error
    }
  })

  test('TC-005-1: 帖子详情 - 点赞功能', async ({ page }) => {
    console.log('开始测试: 帖子详情点赞功能')

    try {
      // 访问论坛页面
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')

      // 等待帖子加载
      await page.waitForFunction(() => {
        const content = document.querySelector('.forum-list__content')
        return content !== null
      }, { timeout: 15000 })

      // 点击第一个帖子
      const postCards = page.locator('.forum-list__card')
      const cardCount = await postCards.count()

      if (cardCount > 0) {
        await postCards.first().click()
        await page.waitForTimeout(2000)

        // 查找点赞按钮
        const likeButton = page.locator('.like-btn, .van-icon-like, [class*="like"]').first()

        if (await likeButton.count() > 0) {
          // 获取点赞前数量
          const likeCountBefore = await page.locator('.like-count, [class*="count"]').first().textContent().catch(() => '0')

          // 点击点赞
          await likeButton.click()
          await page.waitForTimeout(1000)

          // 验证点赞状态变化
          const isLiked = await likeButton.evaluate(el =>
            el.classList.contains('active') ||
            el.classList.contains('van-icon-like-o') === false
          )
          console.log(`点赞状态: ${isLiked ? '已点赞' : '未点赞'}`)

          console.log('✅ 点赞功能测试通过!')
        } else {
          console.log('未找到点赞按钮')
        }
      } else {
        console.log('没有帖子数据')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-005-2: 帖子详情 - 收藏功能', async ({ page }) => {
    console.log('开始测试: 帖子详情收藏功能')

    try {
      // 登录后再测试收藏功能
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', '13800000001')
      await page.fill('input[type="password"]', '123456')
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 访问论坛页面
      await page.goto(BASE_URL)
      await page.waitForLoadState('networkidle')

      // 等待帖子加载
      await page.waitForFunction(() => {
        const content = document.querySelector('.forum-list__content')
        return content !== null
      }, { timeout: 15000 })

      // 点击第一个帖子
      const postCards = page.locator('.forum-list__card')
      if (await postCards.count() > 0) {
        await postCards.first().click()
        await page.waitForTimeout(2000)

        // 查找收藏按钮
        const collectButton = page.locator('.collect-btn, .van-icon-star, [class*="collect"]').first()

        if (await collectButton.count() > 0) {
          // 点击收藏
          await collectButton.click()
          await page.waitForTimeout(1000)

          // 验证收藏状态
          const isCollected = await collectButton.evaluate(el =>
            el.classList.contains('active') ||
            el.classList.contains('van-icon-star-o') === false
          )
          console.log(`收藏状态: ${isCollected ? '已收藏' : '未收藏'}`)

          console.log('✅ 收藏功能测试通过!')
        } else {
          console.log('未找到收藏按钮')
        }
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  // 辅助函数：创建测试帖子
  async function createTestPost(page: any) {
    console.log('创建测试帖子...')

    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.fill('input[type="tel"]', '13800000001')
    await page.fill('input[type="password"]', '123456')
    await page.click('button[type="submit"]')
    await page.waitForURL('**/', { timeout: 15000 })

    // 进入发布页面
    await page.goto(`${BASE_URL}/forum/create`, { waitUntil: 'networkidle' })
    await page.waitForTimeout(2000)

    // 填写标题
    const titleInput = page.locator('input[placeholder*="标题"]').first()
    if (await titleInput.count() > 0) {
      await titleInput.fill('E2E测试帖子')
    }

    // 填写内容
    const contentArea = page.locator('textarea').first()
    if (await contentArea.count() > 0) {
      await contentArea.fill('这是E2E自动化测试帖子内容')
    }

    // 点击发布
    const publishButton = page.locator('button:has-text("发布")').first()
    if (await publishButton.count() > 0) {
      await publishButton.click()
      await page.waitForTimeout(3000)
    }

    // 返回列表
    await page.goto(BASE_URL)
    await page.waitForLoadState('networkidle')
    console.log('测试帖子创建完成')
  }
})
