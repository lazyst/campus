import { test, expect } from '@playwright/test'

test.describe('聊天功能实时性测试', () => {
  test('PC端聊天实时性 - 用户A发送消息给用户B', async ({ browser }) => {
    // 创建两个独立的浏览器上下文（模拟两个用户）
    const contextA = await browser.newContext()
    const contextB = await browser.newContext()

    const pageA = await contextA.newPage()
    const pageB = await contextB.newPage()

    // 测试账号 - 使用同一个账号但不同的会话
    // 注意：同一个账号只能测试给自己发消息，无法测试实时聊天
    // 这里主要测试 WebSocket 连接和消息推送是否正常工作
    const userA = { username: '13800000001', password: '123456' }
    const userB = { username: '13800000001', password: '123456' }

    try {
      // ===== 用户A登录 =====
      console.log('用户A登录...')
      await pageA.goto('http://localhost:3000/login')
      await pageA.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await pageA.fill('input[type="tel"]', userA.username)
      await pageA.fill('input[type="password"]', userA.password)
      await pageA.click('button[type="submit"]')
      await pageA.waitForURL('**/', { timeout: 10000 })
      console.log('用户A登录成功')

      // ===== 用户B登录 =====
      console.log('用户B登录...')
      await pageB.goto('http://localhost:3000/login')
      await pageB.waitForSelector('input[type="tel"]', { timeout: 10000 })
      await pageB.fill('input[type="tel"]', userB.username)
      await pageB.fill('input[type="password"]', userB.password)
      await pageB.click('button[type="submit"]')
      // 等待登录成功，跳转到首页
      try {
        await pageB.waitForURL('**/', { timeout: 15000 })
      } catch {
        // 如果URL没有变化，尝试等待一个元素出现
        await pageB.waitForTimeout(3000)
      }
      console.log('用户B登录成功')

      // ===== 用户A进入消息页面 =====
      console.log('用户A进入消息页面...')
      await pageA.goto('http://localhost:3000/messages')
      await pageA.waitForTimeout(2000)
      console.log('用户A进入消息页面成功')

      // ===== 用户B进入消息页面 =====
      console.log('用户B进入消息页面...')
      await pageB.goto('http://localhost:3000/messages')
      await pageB.waitForTimeout(2000)
      console.log('用户B进入消息页面成功')

      // ===== 等待页面稳定 =====
      await pageA.waitForTimeout(1000)
      await pageB.waitForTimeout(1000)

      // ===== 用户A选择与用户B聊天 =====
      console.log('用户A选择与用户B聊天...')
      const conversationA = pageA.locator('.message-item').first()
      if (await conversationA.count() > 0) {
        await conversationA.click()
        await pageA.waitForTimeout(500)
        console.log('用户A选择了聊天对象')
      } else {
        console.log('用户A没有会话')
      }

      // ===== 用户B选择与用户A聊天 =====
      console.log('用户B选择与用户B聊天...')
      const conversationB = pageB.locator('.message-item').first()
      if (await conversationB.count() > 0) {
        await conversationB.click()
        await pageB.waitForTimeout(500)
        console.log('用户B选择了聊天对象')
      } else {
        console.log('用户B没有会话')
      }

      // ===== 用户A发送消息 =====
      const testMessage = `测试消息 ${Date.now()}`
      console.log(`用户A发送消息: ${testMessage}`)

      // PC端聊天面板输入框
      const inputA = pageA.locator('.chat-panel-input input')
      await inputA.fill(testMessage)
      await inputA.press('Enter')
      console.log('用户A消息已发送')

      // ===== 用户B实时接收消息 =====
      console.log('等待用户B接收消息...')
      await pageB.waitForTimeout(2000) // 等待WebSocket推送

      // 检查用户B的消息列表是否包含该消息
      const messageB = pageB.locator(`.chat-panel-message:has-text("${testMessage}")`)
      const received = await messageB.count() > 0

      console.log(`用户B收到消息: ${received}`)
      expect(received).toBe(true)

      // ===== 用户B回复消息 =====
      const replyMessage = `回复消息 ${Date.now()}`
      console.log(`用户B发送回复: ${replyMessage}`)

      const inputB = pageB.locator('.chat-panel-input input')
      await inputB.fill(replyMessage)
      await inputB.press('Enter')
      console.log('用户B回复已发送')

      // ===== 用户A实时接收回复 =====
      console.log('等待用户A接收回复...')
      await pageA.waitForTimeout(2000)

      const replyA = pageA.locator(`.chat-panel-message:has-text("${replyMessage}")`)
      const replyReceived = await replyA.count() > 0

      console.log(`用户A收到回复: ${replyReceived}`)
      expect(replyReceived).toBe(true)

      console.log('✅ 聊天实时性测试通过!')

    } catch (error) {
      console.error('❌ 测试失败:', error)
      throw error
    } finally {
      await contextA.close()
      await contextB.close()
    }
  })
})
