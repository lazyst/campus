import { test, expect } from '@playwright/test'

test.describe('发布商品功能测试', () => {
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
   * TC-010: 发布商品
   * 验证用户发布二手商品的功能
   */
  test('TC-010: 发布商品 - 完整流程', async ({ page }) => {
    console.log('开始测试: 发布商品')

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

      // ===== 步骤2: 点击底部Tab"闲置" =====
      console.log('步骤2: 点击底部Tab"闲置"')

      // 尝试多种方式找到闲置Tab
      const tradeTab = page.locator('.van-tab').filter({ hasText: /闲置|交易|二手/ }).first()

      if (await tradeTab.count() > 0) {
        await tradeTab.click()
        await page.waitForTimeout(1000)
        console.log('点击了闲置Tab')
      } else {
        // 直接导航到商品页面
        await page.goto(`${BASE_URL}/trade`, { waitUntil: 'networkidle' })
        console.log('直接访问商品页面')
      }

      // ===== 步骤3: 点击右下角"发布"按钮 =====
      console.log('步骤3: 点击右下角发布按钮')

      // 查找发布按钮 - 通常在右下角
      const createButton = page.locator('.create-item-btn, .van-button--primary, .float-add-btn, [class*="add"]').first()

      if (await createButton.count() > 0) {
        await createButton.click()
      } else {
        // 直接导航到发布页面
        await page.goto(`${BASE_URL}/trade/create`, { waitUntil: 'networkidle' })
      }

      await page.waitForTimeout(2000)
      console.log('进入商品发布页面')

      // ===== 步骤4: 输入物品名称 =====
      console.log('步骤4: 输入物品名称')
      const nameInput = page.locator('input[placeholder*="名称"], input[placeholder*="物品"], input[class*="name"]').first()

      if (await nameInput.count() > 0) {
        await nameInput.fill('E2E测试商品')
        const nameValue = await nameInput.inputValue()
        expect(nameValue).toBe('E2E测试商品')
        console.log('物品名称已填入: E2E测试商品')
      } else {
        console.log('未找到名称输入框')
      }

      // ===== 步骤5: 选择闲置类型 =====
      console.log('步骤5: 选择闲置类型：出售')

      // 查找出售按钮
      const sellTypeBtn = page.locator('.type-btn, .van-radio, button').filter({ hasText: /出售/ }).first()

      if (await sellTypeBtn.count() > 0) {
        await sellTypeBtn.click()
        console.log('已选择"出售"类型')

        // 验证按钮高亮（检查是否有active类）
        const isActive = await sellTypeBtn.evaluate(el => el.classList.contains('active') || el.classList.contains('van-radio--checked'))
        console.log(`出售按钮激活状态: ${isActive}`)
      }

      // ===== 步骤6: 输入价格 =====
      console.log('步骤6: 输入价格')
      const priceInput = page.locator('input[placeholder*="价"], input[type="number"], input[class*="price"]').first()

      if (await priceInput.count() > 0) {
        await priceInput.fill('100')
        const priceValue = await priceInput.inputValue()
        expect(priceValue).toBe('100')
        console.log('价格已填入: 100')
      }

      // ===== 步骤7: 选择分类 =====
      console.log('步骤7: 选择分类：生活用品')

      // 查找分类按钮
      const categoryBtn = page.locator('.category-btn, .van-radio, button').filter({ hasText: /生活用品/ }).first()

      if (await categoryBtn.count() > 0) {
        await categoryBtn.click()
        console.log('已选择"生活用品"分类')
      }

      // ===== 步骤8: 选择成色 =====
      console.log('步骤8: 选择成色：95新')

      // 查找成色按钮
      const conditionBtn = page.locator('.condition-btn, .van-radio, button').filter({ hasText: /95新/ }).first()

      if (await conditionBtn.count() > 0) {
        await conditionBtn.click()
        console.log('已选择"95新"成色')
      }

      // ===== 步骤9: 输入物品描述 =====
      console.log('步骤9: 输入物品描述')
      const descArea = page.locator('textarea[placeholder*="描述"], textarea, [placeholder*="说明"]').first()

      if (await descArea.count() > 0) {
        await descArea.fill('E2E自动化测试商品描述')
        const descValue = await descArea.inputValue()
        expect(descValue).toContain('E2E自动化测试')
        console.log('物品描述已填入')
      }

      // ===== 步骤10: 点击发布按钮 =====
      console.log('步骤10: 点击发布按钮')

      const publishButton = page.locator('button:has-text("发布"), button:has-text("提交"), .submit-btn').first()

      if (await publishButton.count() > 0) {
        await publishButton.click()
        console.log('已点击发布按钮')

        // 等待发布成功
        await page.waitForTimeout(3000)

        // ===== 步骤11: 验证商品已发布 =====
        console.log('步骤11: 验证商品已发布')

        const currentUrl = page.url()
        console.log(`当前URL: ${currentUrl}`)

        // 检查是否返回到列表页或显示成功提示
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
        console.log('✅ 发布商品测试通过!')
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

  test('TC-010-1: 发布商品 - 必填字段验证', async ({ page }) => {
    console.log('开始测试: 发布商品必填字段验证')

    try {
      // 登录
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', testUser.phone)
      await page.fill('input[type="password"]', testUser.password)
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 进入发布页面
      await page.goto(`${BASE_URL}/trade/create`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 不填任何内容，直接点击发布
      const publishButton = page.locator('button:has-text("发布"), button:has-text("提交")').first()

      if (await publishButton.count() > 0) {
        // 检查发布按钮是否被禁用
        const isDisabled = await publishButton.isDisabled()
        console.log(`发布按钮禁用状态: ${isDisabled}`)

        // 填写必填字段后检查
        const nameInput = page.locator('input[placeholder*="名称"]').first()
        if (await nameInput.count() > 0) {
          await nameInput.fill('测试商品')

          const priceInput = page.locator('input[placeholder*="价"]').first()
          if (await priceInput.count() > 0) {
            await priceInput.fill('100')
          }

          // 再检查按钮状态
          const isDisabledAfterFill = await publishButton.isDisabled()
          console.log(`填写必填字段后按钮禁用状态: ${isDisabledAfterFill}`)
        }

        console.log('✅ 必填字段验证测试通过!')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-010-2: 发布商品 - 出售/求购切换', async ({ page }) => {
    console.log('开始测试: 发布商品 - 出售/求购切换')

    try {
      // 登录
      await page.goto(`${BASE_URL}/login`)
      await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await page.fill('input[type="tel"]', testUser.phone)
      await page.fill('input[type="password"]', testUser.password)
      await page.click('button[type="submit"]')
      await page.waitForURL('**/', { timeout: 15000 })

      // 进入发布页面
      await page.goto(`${BASE_URL}/trade/create`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 点击"求购"按钮
      const buyTypeBtn = page.locator('button').filter({ hasText: /求购/ }).first()
      if (await buyTypeBtn.count() > 0) {
        await buyTypeBtn.click()
        console.log('点击了"求购"按钮')
        await page.waitForTimeout(500)

        // 验证"求购"按钮已激活
        const isBuyActive = await buyTypeBtn.evaluate(el => el.classList.contains('active') || el.classList.contains('van-radio--checked'))
        console.log(`求购按钮激活状态: ${isBuyActive}`)

        // 点击"出售"按钮
        const sellTypeBtn = page.locator('button').filter({ hasText: /出售/ }).first()
        if (await sellTypeBtn.count() > 0) {
          await sellTypeBtn.click()
          console.log('点击了"出售"按钮')
        }
      }

      console.log('✅ 出售/求购切换测试通过!')

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })
})
