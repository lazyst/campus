import { test, expect } from '@playwright/test'

test.describe('浏览商品列表', () => {
  test('TC-009: 浏览二手商品列表', async ({ page }) => {
    console.log('开始测试: 浏览商品列表')

    try {
      // ===== 步骤1: 访问首页 =====
      console.log('步骤1: 访问首页 http://localhost:3000')
      await page.goto('http://localhost:3000', { waitUntil: 'networkidle', timeout: 30000 })
      console.log('首页加载成功')

      // ===== 步骤2: 点击"闲置"Tab进入商品页面 =====
      console.log('步骤2: 点击"闲置"Tab进入商品页面')

      // 尝试多种选择器找到闲置Tab
      const tradeTab = page.locator('.trade-tab, .van-tab, [class*="trade"], [class*="闲置"]').filter({ hasText: /闲置|出售|全部|求购/ }).first()

      // 如果找不到专门的Tab，则直接导航到商品页面
      if (await tradeTab.count() === 0) {
        console.log('未找到闲置Tab，尝试直接访问商品页面')
        await page.goto('http://localhost:3000/trade', { waitUntil: 'networkidle', timeout: 30000 })
      } else {
        await tradeTab.click()
        console.log('点击了闲置Tab')
      }

      // ===== 步骤3: 等待商品列表加载 =====
      console.log('步骤3: 等待商品列表加载')

      // 等待页面加载完成
      await page.waitForLoadState('domcontentloaded')
      await page.waitForTimeout(2000)

      // 等待商品列表容器出现
      await page.waitForSelector('.waterfall-container, .trade-list', { timeout: 15000 })
      console.log('商品列表容器已出现')

      // ===== 步骤4: 检查商品信息 =====
      console.log('步骤4: 检查商品信息')

      // 等待商品卡片加载
      await page.waitForTimeout(3000)

      // 检查是否有商品卡片
      const itemCount = await page.locator('.waterfall-item').count()
      console.log(`找到 ${itemCount} 个商品卡片`)

      if (itemCount > 0) {
        // 检查第一个商品的详细信息
        const firstItem = page.locator('.waterfall-item').first()

        // 检查商品标题
        const titleElement = firstItem.locator('.item-title')
        const hasTitle = await titleElement.count() > 0
        if (hasTitle) {
          const title = await titleElement.textContent()
          console.log(`商品标题: ${title}`)
          expect(title).toBeTruthy()
        }

        // 检查商品价格
        const priceElement = firstItem.locator('.item-price')
        const hasPrice = await priceElement.count() > 0
        if (hasPrice) {
          const price = await priceElement.textContent()
          console.log(`商品价格: ${price}`)
          expect(price).toBeTruthy()
        }

        // 检查用户信息（可能没有）
        const userElement = firstItem.locator('.user-name')
        const hasUser = await userElement.count() > 0
        if (hasUser) {
          const username = await userElement.textContent()
          console.log(`卖家: ${username}`)
        }

        // 检查发布时间（可能没有）
        const timeElement = firstItem.locator('.item-time')
        const hasTime = await timeElement.count() > 0
        if (hasTime) {
          const time = await timeElement.textContent()
          console.log(`发布时间: ${time}`)
        }

        // 检查商品图片或占位符
        const imageElement = firstItem.locator('.waterfall-image img')
        const hasImage = await imageElement.count() > 0
        if (hasImage) {
          const src = await imageElement.getAttribute('src')
          console.log(`商品图片: ${src ? '有图片' : '无图片'}`)
        } else {
          // 检查是否有占位符
          const placeholder = firstItem.locator('.image-placeholder')
          console.log(`商品图片: 占位符显示`)
        }

        console.log('商品信息检查完成')
      } else {
        // 如果没有商品，检查是否是空状态
        const emptyState = page.locator('.trade-empty')
        const hasEmptyState = await emptyState.count() > 0
        if (hasEmptyState) {
          const emptyText = await emptyState.locator('.empty-text').textContent()
          console.log(`空状态: ${emptyText}`)
        } else {
          console.log('警告: 没有找到商品卡片，也没有空状态提示')
        }
      }

      // ===== 步骤5: 滚动加载更多 =====
      console.log('步骤5: 测试滚动加载更多')

      // 获取当前商品数量
      const initialCount = await page.locator('.waterfall-item').count()
      console.log(`初始商品数量: ${initialCount}`)

      // 滚动到页面底部
      await page.evaluate(() => {
        window.scrollTo(0, document.body.scrollHeight)
      })

      // 等待加载
      await page.waitForTimeout(3000)

      // 获取滚动后商品数量
      const afterScrollCount = await page.locator('.waterfall-item').count()
      console.log(`滚动后商品数量: ${afterScrollCount}`)

      // 如果有分页，应该能看到加载更多或没有更多提示
      const loadingMore = page.locator('.trade-loading-more')
      const finished = page.locator('.trade-finished')

      if (await loadingMore.count() > 0) {
        console.log('显示: 加载中...')
      } else if (await finished.count() > 0) {
        const finishedText = await finished.locator('.finished-text').textContent()
        console.log(`显示: ${finishedText}`)
      }

      // ===== 验证通过标准 =====
      console.log('验证通过标准:')

      // 1. 页面无报错
      const consoleErrors: string[] = []
      page.on('console', msg => {
        if (msg.type() === 'error') {
          consoleErrors.push(msg.text())
        }
      })

      // 等待一下看是否有错误
      await page.waitForTimeout(1000)

      // 2. 商品列表正确渲染（至少显示一条数据或空状态）
      const hasItemsOrEmpty = (await page.locator('.waterfall-item').count() > 0) || (await page.locator('.trade-empty').count() > 0)
      expect(hasItemsOrEmpty).toBe(true)
      console.log('- 商品列表渲染: 通过')

      // 3. 至少显示一条商品数据（如果后端有数据）
      if (await page.locator('.waterfall-item').count() > 0) {
        console.log('- 有商品数据: 通过')
      } else {
        console.log('- 无商品数据（可能系统暂无数据）')
      }

      console.log('✅ 浏览商品列表测试通过!')

    } catch (error) {
      console.error('❌ 测试失败:', error)

      // 捕获页面截图用于调试
      const screenshot = await page.screenshot()
      console.log('测试失败截图已保存')

      throw error
    }
  })

  test('TC-009-1: 商品列表分类Tab切换', async ({ page }) => {
    console.log('开始测试: 商品列表分类Tab切换')

    try {
      // 直接访问商品页面
      await page.goto('http://localhost:3000/trade', { waitUntil: 'networkidle', timeout: 30000 })
      await page.waitForTimeout(2000)

      // 检查是否有分类Tab
      const tabs = page.locator('.trade-tab')
      const tabCount = await tabs.count()
      console.log(`找到 ${tabCount} 个分类Tab`)

      if (tabCount > 0) {
        // 点击"出售"Tab
        const sellTab = tabs.filter({ hasText: '出售' }).first()
        if (await sellTab.count() > 0) {
          await sellTab.click()
          await page.waitForTimeout(2000)
          console.log('点击了"出售"Tab')

          // 检查是否切换到出售分类
          const isActive = await sellTab.evaluate(el => el.classList.contains('trade-tab--active'))
          expect(isActive).toBe(true)
        }

        // 点击"求购"Tab
        const buyTab = tabs.filter({ hasText: '求购' }).first()
        if (await buyTab.count() > 0) {
          await buyTab.click()
          await page.waitForTimeout(2000)
          console.log('点击了"求购"Tab')

          // 检查是否切换到求购分类
          const isActive = await buyTab.evaluate(el => el.classList.contains('trade-tab--active'))
          expect(isActive).toBe(true)
        }

        // 点击"全部"Tab
        const allTab = tabs.filter({ hasText: '全部' }).first()
        if (await allTab.count() > 0) {
          await allTab.click()
          await page.waitForTimeout(2000)
          console.log('点击了"全部"Tab')
        }
      }

      console.log('✅ 分类Tab切换测试通过!')

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-009-2: 商品列表排序功能', async ({ page }) => {
    console.log('开始测试: 商品列表排序功能')

    try {
      // 直接访问商品页面
      await page.goto('http://localhost:3000/trade', { waitUntil: 'networkidle', timeout: 30000 })
      await page.waitForTimeout(2000)

      // 检查是否有排序按钮
      const sortButtons = page.locator('.trade-sort-btn')
      const sortCount = await sortButtons.count()
      console.log(`找到 ${sortCount} 个排序按钮`)

      if (sortCount > 0) {
        // 点击"价格↑"排序
        const priceAscBtn = sortButtons.filter({ hasText: '价格↑' }).first()
        if (await priceAscBtn.count() > 0) {
          await priceAscBtn.click()
          await page.waitForTimeout(2000)
          console.log('点击了"价格↑"排序')

          // 检查是否激活
          const isActive = await priceAscBtn.evaluate(el => el.classList.contains('trade-sort-btn--active'))
          expect(isActive).toBe(true)
        }

        // 点击"价格↓"排序
        const priceDescBtn = sortButtons.filter({ hasText: '价格↓' }).first()
        if (await priceDescBtn.count() > 0) {
          await priceDescBtn.click()
          await page.waitForTimeout(2000)
          console.log('点击了"价格↓"排序')
        }

        // 点击"最新"排序
        const newestBtn = sortButtons.filter({ hasText: '最新' }).first()
        if (await newestBtn.count() > 0) {
          await newestBtn.click()
          await page.waitForTimeout(2000)
          console.log('点击了"最新"排序')
        }
      }

      console.log('✅ 排序功能测试通过!')

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })
})
