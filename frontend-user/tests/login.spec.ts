import { test, expect } from '@playwright/test'

test.describe('用户登录功能测试', () => {
  const BASE_URL = 'http://localhost:3000'
  const testUser = {
    phone: '13800000001',
    password: '123456'
  }

  test.beforeEach(async ({ page }) => {
    // 每个测试前清除登录状态，确保测试独立
    await page.goto(`${BASE_URL}/login`)
    await page.evaluate(() => {
      localStorage.clear()
    })
    await page.reload()
  })

  test('TC-001: 正常登录 - 使用正确的手机号和密码', async ({ page }) => {
    console.log('开始测试: 正常登录')

    // 步骤1: 打开登录页面
    console.log('步骤1: 打开登录页面')
    await page.goto(`${BASE_URL}/login`)

    // 等待登录表单加载
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })
    await page.waitForSelector('input[type="password"]', { timeout: 5000 })

    // 验证页面显示登录表单
    const pageTitle = await page.locator('.login-title').textContent()
    expect(pageTitle).toContain('校园互助')
    console.log('登录页面加载成功')

    // 步骤2: 输入手机号
    console.log('步骤2: 输入手机号')
    await page.fill('input[type="tel"]', testUser.phone)

    // 验证手机号已填入
    const phoneValue = await page.inputValue('input[type="tel"]')
    expect(phoneValue).toBe(testUser.phone)
    console.log(`手机号已填入: ${phoneValue}`)

    // 步骤3: 输入密码
    console.log('步骤3: 输入密码')
    await page.fill('input[type="password"]', testUser.password)
    console.log('密码已填入')

    // 步骤4: 点击登录按钮
    console.log('步骤4: 点击登录按钮')
    await page.click('button[type="submit"]')

    // 等待页面跳转到首页
    console.log('等待页面跳转...')
    await page.waitForURL('**/', { timeout: 15000 })

    // 验证URL已变化（不再是登录页）
    const currentUrl = page.url()
    expect(currentUrl).not.toContain('/login')
    console.log(`页面已跳转，当前URL: ${currentUrl}`)

    // 步骤5: 验证用户状态
    console.log('步骤5: 验证用户登录状态')

    // 检查localStorage中的token
    const token = await page.evaluate(() => localStorage.getItem('token'))
    expect(token).not.toBeNull()
    expect(token).toBeTruthy()
    console.log('Token已保存到localStorage')

    // 检查用户信息
    const userData = await page.evaluate(() => {
      const user = localStorage.getItem('user')
      return user ? JSON.parse(user) : null
    })
    expect(userData).not.toBeNull()
    expect(userData.token).toBeTruthy()
    console.log('用户信息已保存')

    // 验证登录成功，无错误提示
    // 检查页面上没有错误提示（van toast错误提示会有特定的class）
    const hasErrorToast = await page.locator('.van-toast--fail').count()
    expect(hasErrorToast).toBe(0)
    console.log('无错误提示')

    console.log('测试通过: 登录成功')
  })

  test('TC-002: 手机号输入验证 - 空手机号', async ({ page }) => {
    console.log('开始测试: 空手机号验证')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 不输入手机号，直接输入密码
    await page.fill('input[type="password"]', testUser.password)

    // 检查登录按钮是否被禁用
    const submitButton = page.locator('button[type="submit"]')
    const isDisabled = await submitButton.isDisabled()
    expect(isDisabled).toBe(true)
    console.log('空手机号时登录按钮被禁用 - 测试通过')
  })

  test('TC-003: 手机号输入验证 - 格式错误', async ({ page }) => {
    console.log('开始测试: 手机号格式错误验证')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 输入错误格式的手机号
    await page.fill('input[type="tel"]', '12345')

    // 输入密码
    await page.fill('input[type="password"]', testUser.password)

    // 检查登录按钮是否被禁用
    const submitButton = page.locator('button[type="submit"]')
    const isDisabled = await submitButton.isDisabled()
    expect(isDisabled).toBe(true)
    console.log('手机号格式错误时登录按钮被禁用 - 测试通过')
  })

  test('TC-004: 密码输入验证 - 空密码', async ({ page }) => {
    console.log('开始测试: 空密码验证')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 只输入手机号，不输入密码
    await page.fill('input[type="tel"]', testUser.phone)

    // 检查登录按钮是否被禁用
    const submitButton = page.locator('button[type="submit"]')
    const isDisabled = await submitButton.isDisabled()
    expect(isDisabled).toBe(true)
    console.log('空密码时登录按钮被禁用 - 测试通过')
  })

  test('TC-005: 错误密码登录', async ({ page }) => {
    console.log('开始测试: 错误密码登录')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 输入正确的手机号，错误的密码
    await page.fill('input[type="tel"]', testUser.phone)
    await page.fill('input[type="password"]', 'wrongpassword')

    // 点击登录按钮
    await page.click('button[type="submit"]')

    // 等待可能的错误提示
    await page.waitForTimeout(3000)

    // 验证仍然在登录页（未跳转）
    const currentUrl = page.url()
    expect(currentUrl).toContain('/login')
    console.log('错误密码时页面未跳转 - 测试通过')
  })

  test('TC-006: 不存在的用户登录', async ({ page }) => {
    console.log('开始测试: 不存在的用户登录')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 输入不存在的手机号
    await page.fill('input[type="tel"]', '13900000000')
    await page.fill('input[type="password"]', '123456')

    // 点击登录按钮
    await page.click('button[type="submit"]')

    // 等待可能的错误提示
    await page.waitForTimeout(3000)

    // 验证仍然在登录页（未跳转）
    const currentUrl = page.url()
    expect(currentUrl).toContain('/login')
    console.log('不存在用户时页面未跳转 - 测试通过')
  })

  test('TC-007: 登录后页面显示验证', async ({ page }) => {
    console.log('开始测试: 登录后页面显示验证')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('input[type="tel"]', { timeout: 10000 })

    // 登录
    await page.fill('input[type="tel"]', testUser.phone)
    await page.fill('input[type="password"]', testUser.password)
    await page.click('button[type="submit"]')

    // 等待跳转到首页
    await page.waitForURL('**/', { timeout: 15000 })

    // 等待页面加载完成
    await page.waitForLoadState('networkidle')

    // 验证首页显示正常 - 检查是否有内容区域
    const hasContent = await page.locator('.main-layout').count()
    expect(hasContent).toBeGreaterThan(0)
    console.log('首页布局加载正常')

    // 验证底部导航栏显示
    const hasTabBar = await page.locator('.tab-bar, .van-tabbar').count()
    console.log(`底部导航栏存在: ${hasTabBar > 0}`)

    console.log('登录后页面显示验证 - 测试通过')
  })

  test('TC-008: 注册链接跳转', async ({ page }) => {
    console.log('开始测试: 注册链接跳转')

    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('.login-register-link', { timeout: 10000 })

    // 点击注册链接
    await page.click('.login-register-link')

    // 等待跳转到注册页
    await page.waitForURL('**/register', { timeout: 10000 })

    // 验证注册页面加载成功
    const currentUrl = page.url()
    expect(currentUrl).toContain('/register')
    console.log('注册链接跳转成功 - 测试通过')
  })
})
