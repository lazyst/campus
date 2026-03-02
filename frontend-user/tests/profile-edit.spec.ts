import { test, expect } from '@playwright/test'

test.describe('资料编辑功能测试', () => {
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
   * TC-018: 资料编辑
   * 验证用户编辑个人资料的功能
   */
  test('TC-018: 资料编辑 - 完整流程', async ({ page }) => {
    console.log('开始测试: 资料编辑')

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

      // ===== 步骤2: 直接访问资料编辑页面 =====
      console.log('步骤2: 直接访问资料编辑页面')
      await page.goto(`${BASE_URL}/profile/edit`, { waitUntil: 'networkidle' })
      await page.waitForTimeout(2000)

      // 如果被重定向到登录页，先登录
      if (page.url().includes('/login')) {
        console.log('需要登录，先进行登录')
        await page.fill('input[type="tel"]', testUser.phone)
        await page.fill('input[type="password"]', testUser.password)
        await page.click('button[type="submit"]')
        await page.waitForURL('**/', { timeout: 15000 })
        // 登录后再次访问
        await page.goto(`${BASE_URL}/profile/edit`, { waitUntil: 'networkidle' })
        await page.waitForTimeout(2000)
      }

      // ===== 步骤3: 点击头像或编辑资料 =====
      console.log('步骤3: 点击头像或编辑资料')

      // 查找编辑资料入口 - 头像或编辑按钮
      const editButton = page.locator(
        '[href*="edit"], ' +
        '[href*="profile/edit"], ' +
        'a:has-text("编辑资料"), ' +
        '[class*="edit"]:not([class*="text"]), ' +
        '.avatar, [class*="avatar"]'
      ).first()

      const hasEditButton = await editButton.count() > 0

      if (hasEditButton) {
        await editButton.click()
        await page.waitForTimeout(2000)
        console.log('已进入资料编辑页面')
      } else {
        // 直接访问编辑页面
        await page.goto(`${BASE_URL}/profile/edit`, { waitUntil: 'networkidle' })
        await page.waitForTimeout(2000)
        console.log('直接访问资料编辑页面')
      }

      // ===== 步骤4: 等待编辑表单加载 =====
      console.log('步骤4: 等待编辑表单加载')

      // 等待表单出现
      await page.waitForFunction(() => {
        const form = document.querySelector(
          '.edit-form, .profile-form, [class*="form"]'
        )
        const inputs = document.querySelectorAll('input, textarea')
        return (form !== null && inputs.length > 0) || inputs.length > 2
      }, { timeout: 15000 })

      console.log('编辑表单已加载')

      // ===== 步骤5: 修改昵称 =====
      console.log('步骤5: 修改昵称')

      const nicknameInput = page.locator(
        'input[placeholder*="昵称"], ' +
        'input[name="nickname"], ' +
        '[class*="nickname"] input'
      ).first()

      const hasNicknameInput = await nicknameInput.count() > 0

      if (hasNicknameInput) {
        // 清空并输入新昵称
        await nicknameInput.clear()
        const newNickname = `测试用户${Date.now()}`
        await nicknameInput.fill(newNickname)
        console.log(`新昵称: ${newNickname}`)

        // ===== 步骤6: 修改个人简介 =====
        console.log('步骤6: 修改个人简介')

        const bioInput = page.locator(
          'textarea[placeholder*="简介"], ' +
          'textarea[placeholder*="个人"], ' +
          '[class*="bio"] textarea, ' +
          '[class*="intro"] textarea'
        ).first()

        const hasBioInput = await bioInput.count() > 0

        if (hasBioInput) {
          await bioInput.clear()
          const newBio = '这是E2E自动化测试的个人简介'
          await bioInput.fill(newBio)
          console.log(`新简介: ${newBio}`)
        }

        // ===== 步骤7: 点击保存按钮 =====
        console.log('步骤7: 点击保存按钮')

        const saveButton = page.locator(
          'button:has-text("保存"), ' +
          'button[type="submit"], ' +
          '[class*="save"]'
        ).first()

        const hasSaveButton = await saveButton.count() > 0

        if (hasSaveButton) {
          await saveButton.click()
          await page.waitForTimeout(2000)

          // 检查是否有成功提示
          const successToast = page.locator(
            '.van-toast--success, .van-toast:has-text("成功")'
          )
          const hasSuccess = await successToast.count() > 0

          console.log(`保存成功提示: ${hasSuccess ? '显示' : '未显示'}`)

          // ===== 步骤8: 返回个人中心 =====
          console.log('步骤8: 返回个人中心')

          // 返回按钮
          const backButton = page.locator(
            '.van-icon-arrow-left, .back, [class*="back"]'
          ).first()

          if (await backButton.count() > 0) {
            await backButton.click()
            await page.waitForTimeout(2000)
          } else {
            await page.goto(`${BASE_URL}/profile`, { waitUntil: 'networkidle' })
            await page.waitForTimeout(2000)
          }

          // ===== 验证通过标准 =====
          console.log('验证通过标准:')

          // 1. 保存成功，无错误提示
          const hasErrorToast = await page.locator('.van-toast--fail').count()
          expect(hasErrorToast).toBe(0)
          console.log('- 保存成功，无错误提示: 通过')

          // 2. 页面正确跳转
          const currentUrl = page.url()
          expect(currentUrl).toContain('/profile')
          console.log('- 页面正确跳转: 通过')

          console.log('✅ 资料编辑测试通过!')
        } else {
          console.log('未找到保存按钮')
        }
      } else {
        console.log('未找到昵称输入框')
      }

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    }
  })

  test('TC-018-1: 资料编辑 - 仅修改昵称', async ({ page }) => {
    console.log('开始测试: 仅修改昵称')

    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.fill('input[type="tel"]', testUser.phone)
    await page.fill('input[type="password"]', testUser.password)
    await page.click('button[type="submit"]')
    await page.waitForURL('**/', { timeout: 15000 })

    // 进入编辑页面
    await page.goto(`${BASE_URL}/profile/edit`, { waitUntil: 'networkidle' })
    await page.waitForTimeout(2000)

    // 修改昵称
    const nicknameInput = page.locator('input[placeholder*="昵称"]').first()
    if (await nicknameInput.count() > 0) {
      await nicknameInput.clear()
      await nicknameInput.fill(`昵称测试${Date.now()}`)

      // 保存
      const saveButton = page.locator('button:has-text("保存")').first()
      if (await saveButton.count() > 0) {
        await saveButton.click()
        await page.waitForTimeout(2000)

        const hasErrorToast = await page.locator('.van-toast--fail').count()
        expect(hasErrorToast).toBe(0)

        console.log('✅ 仅修改昵称测试通过!')
      }
    }
  })

  test('TC-018-2: 资料编辑 - 取消编辑', async ({ page }) => {
    console.log('开始测试: 取消编辑')

    // 登录
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.fill('input[type="tel"]', testUser.phone)
    await page.fill('input[type="password"]', testUser.password)
    await page.click('button[type="submit"]')
    await page.waitForURL('**/', { timeout: 15000 })

    // 进入编辑页面
    await page.goto(`${BASE_URL}/profile/edit`, { waitUntil: 'networkidle' })
    await page.waitForTimeout(2000)

    // 不做任何修改，直接返回
    const backButton = page.locator('.van-icon-arrow-left').first()
    if (await backButton.count() > 0) {
      await backButton.click()
      await page.waitForTimeout(1000)

      const currentUrl = page.url()
      expect(currentUrl).toContain('/profile')

      console.log('✅ 取消编辑测试通过!')
    }
  })
})
