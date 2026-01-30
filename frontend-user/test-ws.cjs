const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();

  page.on('console', msg => {
    console.log(`[${msg.type()}] ${msg.text()}`);
  });

  page.on('pageerror', error => {
    console.log(`[page error] ${error.message}`);
  });

  try {
    console.log('打开聊天页面...');
    await page.goto('http://localhost:3000/messages/8', { waitUntil: 'networkidle', timeout: 30000 });

    // 等待WebSocket连接
    await page.waitForTimeout(5000);

    // 检查页面元素
    const title = await page.locator('.chat-title').textContent().catch(() => '未找到');
    console.log(`聊天标题: ${title}`);

    const inputExists = await page.locator('.message-input').count();
    console.log(`输入框存在: ${inputExists > 0}`);

    // 截图
    await page.screenshot({ path: 'chat-test.png', fullPage: true });
    console.log('截图已保存到 chat-test.png');

  } catch (error) {
    console.error('错误:', error.message);
  }

  console.log('浏览器保持打开...');
})();
