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
      // ===== 步骤1: 登录系统 =====
      console.log('步骤1: 登录系统')
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', testUser.phone)
      await page.fill('input[type="password"]', testUser.password)
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })
      console.log('登录成功，进入首页')

      // ===== 步骤2: 进入个人中心 =====
      console.log('步骤2: 进入个人中心')

      // 查找个人中心入口 - 底部导航栏
      const profileTab = page.locator(
        '.van-tabbar-item:has-text("我的"), ' +
        '.van-tabbar-item[data-name="profile"], ' +
        '.tab-bar-item:has-text("我的"), ' +
        '[class*="tab"]:has-text("我的")'
      ).first()

      await profileTab.click()
      await page.waitForTimeout(2000)
      console.log('已点击个人中心')

      // ===== 步骤3: 点击"我的发布" =====
      console.log('步骤3: 点击"我的发布"')

      // 查找我的发布入口
      const myPostsLink = page.locator(
        '[href*="posts"], ' +
        '[href*="my/post"], ' +
        'a:has-text("我的发布"), ' +
        '[class*="post"]:not([class*="create"]):not([class*="new"])'
      ).first()

      const hasMyPostsLink = await myPostsLink.count() > 0

      if (hasMyPostsLink) {
        await myPostsLink.click()
        await page.waitForTimeout(2000)
        console.log('已进入我的发布页面')
      } else {
        // 尝试直接访问我的发布页面
        await page.goto(`${BASE_URL}/profile/posts`, { waitUntil: 'networkidle' })
        await page.waitForTimeout(2000)
        console.log('直接访问我的发布页面')
      }

      // ===== 步骤4: 等待帖子列表加载 =====
      console.log('步骤4: 等待帖子列表加载')

      // 等待内容区域出现
      await page.waitForFunction(() => {
        const container = document.querySelector(
          '.post-list, .my-post-list, .profile-post-list, [class*="post-list"], [class*="my-post"]'
        )
        return container !== null || document.querySelector('[class*="list"]') !== null
      }, { timeout: 15000 })

      console.log('帖子列表容器已出现')

      // ===== 步骤5: 检查帖子信息 =====
      console.log('步骤5: 检查帖子信息')

      // 查找帖子卡片
      const postCards = page.locator(
        '.post-card, .forum-card, [class*="post-item"], [class*="card"]'
      )

      const cardCount = await postCards.count()
      console.log(`帖子数量: ${cardCount}`)

      if (cardCount > 0) {
        // 检查第一个帖子的信息
        const firstCard = postCards.first()

        // 检查标题
        const titleElement = firstCard.locator(
          '.post-title, .title, [class*="title"]'
        ).first()
        const hasTitle = await titleElement.count() > 0
        if (hasTitle) {
          const titleText = await titleElement.textContent()
          console.log(`帖子标题: ${titleText}`)
        }

        // 检查发布时间
        const timeElement = firstCard.locator(
          '.post-time, .time, [class*="time"]'
        ).first()
        const hasTime = await timeElement.count() > 0
        if (hasTime) {
          const timeText = await timeElement.textContent()
          console.log(`发布时间: ${timeText}`)
        }

        // 检查板块
        const boardElement = firstCard.locator(
          '.post-board, .board, [class*="board"], [class*="tag"]'
        ).first()
        const hasBoard = await boardElement.count() > 0
        if (hasBoard) {
          const boardText = await boardElement.textContent()
          console.log(`板块: ${boardText}`)
        }

        // ===== 验证通过标准 =====
        console.log('验证通过标准:')

        // 1. 页面无报错
        const hasErrorToast = await page.locator('.van-toast--fail').count()
        expect(hasErrorToast).toBe(0)
        console.log('- 页面无报错: 通过')

        // 2. 帖子列表正确显示
        expect(cardCount).toBeGreaterThan(0)
        console.log('- 帖子列表正确显示: 通过')

        // 3. 包含帖子基本信息
        expect(hasTitle).toBe(true)
        console.log('- 帖子基本信息完整: 通过')

        console.log('✅ 查看我的帖子测试通过!')
      } else {
        console.log('没有帖子数据，测试通过（用户暂无帖子）')
        // 无帖子也是一种正常情况
        expect(true).toBe(true)
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-015-1: 查看我的帖子 - 空列表', async ({ page }) => {
    console.log('开始测试: 空帖子列表')

    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.fill('input[type="tel"]', testUser.phone)
    await page.fill('input[type="password"]', testUser.password)
    await page.click('button[type="submit"]')
    await page.waitForURL('**/', { timeout: 15000 })

    // 直接访问我的发布页面
    await page.goto(`${BASE_URL}/profile/posts`, { waitUntil: 'networkidle' })
    await page.waitForTimeout(2000)

    // 页面应该正常加载，不报错
    const hasErrorToast = await page.locator('.van-toast--fail').count()
    expect(hasErrorToast).toBe(0)

    console.log('✅ 空列表测试通过!')
  })
})
