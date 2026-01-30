const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  // 监听控制台消息
  page.on('console', msg => {
    const text = msg.text();
    // 只显示WebSocket相关消息
    if (text.includes('WebSocket') || text.includes('SockJS') || text.includes('STOMP') || text.includes('连接') || text.includes('消息')) {
      console.log(`[${msg.type()}] ${text}`);
    }
  });

  page.on('pageerror', error => {
    console.log(`[page error] ${error.message}`);
  });

  try {
    // 1. 先登录
    console.log('=== 步骤1: 登录用户1 (18475840490) ===');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });

    await page.fill('input[type="tel"]', '18475840490');
    await page.fill('input[type="password"]', '123456');
    await page.click('button.login-submit');

    await page.waitForURL('**/', { timeout: 10000 });
    console.log('登录成功！');

    // 2. 进入聊天页面
    console.log('\n=== 步骤2: 进入聊天页面 ===');
    await page.goto('http://localhost:3000/messages/8', { waitUntil: 'networkidle' });
    await page.waitForTimeout(3000);

    // 检查页面内容
    const chatTitle = await page.locator('.chat-title').textContent().catch(() => '未找到标题');
    console.log(`聊天标题: ${chatTitle}`);

    // 3. 尝试发送消息
    console.log('\n=== 步骤3: 尝试发送消息 ===');
    await page.fill('.message-input', '测试消息1');
    await page.click('.send-btn');
    await page.waitForTimeout(2000);

    // 检查消息是否发送
    const messages = await page.locator('.message-bubble').count();
    console.log(`消息数量: ${messages}`);

    // 4. 打开第二个浏览器窗口测试接收
    console.log('\n=== 步骤4: 使用第二个用户接收消息 ===');
    const page2 = await context.newPage();

    page2.on('console', msg => {
      const text = msg.text();
      if (text.includes('WebSocket') || text.includes('SockJS') || text.includes('STOMP') || text.includes('收到消息')) {
        console.log(`[用户2] [${msg.type()}] ${text}`);
      }
    });

    await page2.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });
    await page2.fill('input[type="tel"]', '19287588725');
    await page2.fill('input[type="password"]', '123456');
    await page2.click('button.login-submit');
    await page2.waitForURL('**/home', { timeout: 10000 });
    console.log('用户2登录成功');

    // 进入与用户1的聊天
    await page2.goto('http://localhost:3000/messages/7', { waitUntil: 'networkidle' });
    await page2.waitForTimeout(3000);

    console.log('\n=== 测试完成 ===');
    console.log('请手动检查两个浏览器窗口的聊天功能');

  } catch (error) {
    console.error('测试失败:', error.message);
  }

  console.log('\n浏览器保持打开以便手动测试...');
})();
