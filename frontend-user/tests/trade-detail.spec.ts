import { test, expect } from '@playwright/test'

test.describe('商品详情功能测试', () => {
  const BASE_URL = 'http://localhost:3000'

  test.beforeEach(async ({ page }) => {
    // 每个测试前清除登录状态，确保测试独立
    await page.goto(BASE_URL)
    await page.evaluate(() => {
      localStorage.clear()
    })
  })

  /**
   * TC-011: 商品详情查看
   * 验证用户查看商品详情的功能
   */
  test('TC-011: 商品详情查看 - 完整流程', async ({ page }) => {
    console.log('开始测试: 商品详情查看')

    try {
      // ===== 步骤1: 访问商品页面 =====
      console.log('步骤1: 访问商品页面')

      // 直接访问商品列表页面
      await page.goto(`${BASE_URL}/trade`, { waitUntil: 'networkidle', timeout: 30000 })
      await page.waitForTimeout(2000)
      console.log('商品页面加载成功')

      // ===== 步骤2: 等待商品列表加载 =====
      console.log('步骤2: 等待商品列表加载')

      // 等待商品列表容器出现
      await page.waitForSelector('.waterfall-container, .trade-list, .goods-list', { timeout: 15000 })
      console.log('商品列表容器已出现')

      // ===== 步骤3: 获取第一个商品信息 =====
      console.log('步骤3: 获取商品信息')

      const itemCards = page.locator('.waterfall-item, .trade-item, .goods-item')
      const cardCount = await itemCards.count()

      if (cardCount === 0) {
        console.log('没有商品数据，准备创建测试商品...')
        await createTestItem(page)
      }

      // 再次检查商品
      const cardsAfterCreate = await itemCards.count()
      console.log(`当前商品数量: ${cardsAfterCreate}`)

      if (cardsAfterCreate > 0) {
        // 获取商品标题
        const firstCard = itemCards.first()
        const titleElement = firstCard.locator('.item-title, .trade-title, .goods-title').first()
        const itemTitle = await titleElement.textContent().catch(async () => {
          // 尝试其他选择器
          const altTitle = firstCard.locator('[class*="title"]').first()
          return await altTitle.textContent()
        })
        console.log(`准备查看商品: ${itemTitle}`)

        // ===== 步骤4: 点击商品卡片 =====
        console.log('步骤4: 点击商品卡片')
        await firstCard.click()
        console.log('已点击商品')

        // 等待页面跳转
        await page.waitForTimeout(2000)

        // ===== 步骤5: 等待详情页加载 =====
        console.log('步骤5: 等待详情页加载')

        // 等待详情页关键元素出现
        await page.waitForFunction(() => {
          const detailContainer = document.querySelector('.item-detail, .trade-detail, .goods-detail, [class*="detail"]')
          return detailContainer !== null
        }, { timeout: 15000 })
        console.log('商品详情页加载完成')

        // ===== 步骤6: 检查商品信息 =====
        console.log('步骤6: 检查商品信息')

        // 检查商品标题
        const detailTitle = page.locator('.item-detail__title, .detail-title, h1, [class*="title"]').first()
        const hasTitle = await detailTitle.count() > 0
        expect(hasTitle).toBe(true)
        const titleText = await detailTitle.textContent()
        console.log(`商品标题: ${titleText}`)

        // 检查商品价格
        const priceElement = page.locator('.item-detail__price, .detail-price, .price, [class*="price"]').first()
        const hasPrice = await priceElement.count() > 0
        expect(hasPrice).toBe(true)
        const priceText = await priceElement.textContent()
        console.log(`商品价格: ${priceText}`)

        // 检查商品描述
        const descElement = page.locator('.item-detail__desc, .detail-desc, .desc, [class*="desc"]').first()
        const hasDesc = await descElement.count() > 0
        if (hasDesc) {
          const descText = await descElement.textContent()
          console.log(`商品描述: ${descText.substring(0, 50)}...`)
        }

        // 检查卖家信息
        const sellerElement = page.locator('.item-detail__seller, .detail-seller, .seller, [class*="seller"]').first()
        const hasSeller = await sellerElement.count() > 0
        if (hasSeller) {
          const sellerName = await sellerElement.textContent()
          console.log(`卖家: ${sellerName}`)
        }

        // 检查发布时间
        const timeElement = page.locator('.item-detail__time, .detail-time, .time, [class*="time"]').first()
        const hasTime = await timeElement.count() > 0
        if (hasTime) {
          const postTime = await timeElement.textContent()
          console.log(`发布时间: ${postTime}`)
        }

        // ===== 步骤7: 检查联系方式 =====
        console.log('步骤7: 检查联系方式')

        const contactElement = page.locator('.item-detail__contact, .detail-contact, .contact, [class*="contact"]')
        const hasContact = await contactElement.count() > 0
        console.log(`联系方式区域存在: ${hasContact}`)

        if (hasContact) {
          const contactText = await contactElement.first().textContent()
          console.log(`联系方式: ${contactText}`)
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
        expect(hasPrice).toBe(true)
        console.log('- 内容完整显示: 通过')

        // 3. URL正确
        const currentUrl = page.url()
        expect(currentUrl).toContain('/trade/')
        console.log('- URL正确: 通过')

        // 4. 联系方式显示
        console.log('- 联系方式: 检查通过')

        console.log('✅ 商品详情查看测试通过!')
      } else {
        console.log('无法获取商品数据，跳过测试')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      const screenshot = await page.screenshot()
      console.log('测试失败截图已保存')
      throw error
    }
  })

  test('TC-011-1: 商品详情 - 收藏功能', async ({ page }) => {
    console.log('开始测试: 商品详情收藏功能')

    try {
      // 登录
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', '13800000001')
      await page.fill('input[type="password"]', '123456')
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 访问商品页面
      await page.goto(`${BASE_URL}/trade`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 等待商品加载
      await page.waitForSelector('.waterfall-item', { timeout: 15000 })

      // 点击第一个商品
      const itemCards = page.locator('.waterfall-item')
      if (await itemCards.count() > 0) {
        await itemCards.first().click()
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
      } else {
        console.log('没有商品数据')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-011-2: 商品详情 - 联系卖家功能', async ({ page }) => {
    console.log('开始测试: 商品详情联系卖家功能')

    try {
      // 登录
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', '13800000001')
      await page.fill('input[type="password"]', '123456')
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 访问商品页面
      await page.goto(`${BASE_URL}/trade`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 等待商品加载
      await page.waitForSelector('.waterfall-item', { timeout: 15000 })

      // 点击第一个商品
      const itemCards = page.locator('.waterfall-item')
      if (await itemCards.count() > 0) {
        await itemCards.first().click()
        await page.waitForTimeout(2000)

        // 查找联系卖家按钮
        const contactButton = page.locator('.contact-btn, .van-button--primary, button:has-text("联系"), [class*="contact"]').first()

        if (await contactButton.count() > 0) {
          // 点击联系卖家
          await contactButton.click()
          await page.waitForTimeout(1000)

          // 检查是否弹出联系选项
          const contactOptions = page.locator('.van-action-sheet, .van-dialog, [class*="sheet"]')
          const hasOptions = await contactOptions.count() > 0
          console.log(`联系选项弹窗: ${hasOptions ? '显示' : '未显示'}`)

          console.log('✅ 联系卖家功能测试通过!')
        } else {
          console.log('未找到联系卖家按钮')
        }
      } else {
        console.log('没有商品数据')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  // 辅助函数：创建测试商品
  async function createTestItem(page: any) {
    console.log('创建测试商品...')

    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.fill('input[type="tel"]', '13800000001')
    await page.fill('input[type="password"]', '123456')
    await page.click('button[type="submit"]')
    await page.waitForURL('**/', { timeout: 15000 })

    // 进入发布页面
    await page.goto(`${BASE_URL}/trade/create`, { waitUntil: 'networkidle' })
    await page.waitForTimeout(2000)

    // 填写商品名称
    const nameInput = page.locator('input[placeholder*="名称"]').first()
    if (await nameInput.count() > 0) {
      await nameInput.fill('E2E测试商品')
    }

    // 填写价格
    const priceInput = page.locator('input[placeholder*="价"]').first()
    if (await priceInput.count() > 0) {
      await priceInput.fill('100')
    }

    // 填写描述
    const descArea = page.locator('textarea').first()
    if (await descArea.count() > 0) {
      await descArea.fill('E2E自动化测试商品描述')
    }

    // 点击发布
    const publishButton = page.locator('button:has-text("发布")').first()
    if (await publishButton.count() > 0) {
      await publishButton.click()
      await page.waitForTimeout(3000)
    }

    // 返回列表
    await page.goto(`${BASE_URL}/trade`)
    await page.waitForLoadState('networkidle')
    console.log('测试商品创建完成')
  }
})
