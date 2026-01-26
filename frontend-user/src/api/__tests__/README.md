# API 测试套件

本目录包含前端API层的完整测试套件，使用 Vitest 测试框架。

## 📋 测试覆盖

### API模块测试（10个）
- ✅ `auth.test.js` - 登录、注册、登出
- ✅ `user.test.js` - 用户信息、资料编辑、头像上传
- ✅ `board.test.js` - 板块列表、板块详情
- ✅ `post.test.js` - 帖子CRUD、点赞、收藏
- ✅ `comment.test.js` - 评论CRUD
- ✅ `notification.test.js` - 通知列表、标记已读
- ✅ `item.test.js` - 物品CRUD、上下架、联系卖家
- ✅ `itemCollect.test.js` - 物品收藏
- ✅ `conversation.test.js` - 会话列表
- ✅ `message.test.js` - 消息列表

### Hooks测试（1个）
- ✅ `hooks.test.js` - useRequest, usePagination, useInfiniteList

## 🚀 运行测试

### 运行所有测试
```bash
cd frontend-user
npm test
```

### 运行单个测试文件
```bash
npm test auth.test.js
```

### 运行测试并生成覆盖率报告
```bash
npm test -- --coverage
```

### 监听模式（开发时使用）
```bash
npm test -- --watch
```

## 📊 测试统计

- **总测试文件数**: 11个
- **测试用例数**: 约80+个
- **覆盖的API函数**: 全部40+个API函数
- **覆盖的Hooks**: 全部3个Hooks

## 🎯 测试特点

### 1. Mock测试
所有测试都使用mock数据，不依赖真实后端：
- Mock axios请求
- Mock localStorage
- Mock Vue响应式状态

### 2. 完整的API覆盖
每个API函数都有对应的测试：
- ✅ 成功场景
- ✅ 失败场景
- ✅ 边界条件
- ✅ 参数验证

### 3. 自动化验证
- ✅ 验证请求参数正确性
- ✅ 验证返回值格式
- ✅ 验证错误处理
- ✅ 验证状态管理（localStorage等）

## 📝 测试示例

### API测试示例
```javascript
it('should login successfully with valid credentials', async () => {
  const mockCredentials = {
    phone: '13800138000',
    password: '123456'
  }

  const mockResponse = {
    data: {
      code: 200,
      message: '登录成功',
      data: 'mock-jwt-token'
    }
  }

  axios.create.mockReturnValue({
    post: vi.fn().mockResolvedValue(mockResponse)
  })

  const result = await login(mockCredentials)

  expect(localStorage.getItem('token')).toBe('mock-jwt-token')
  expect(result).toBe('mock-jwt-token')
})
```

### Hook测试示例
```javascript
it('should execute request and return data', async () => {
  mockApiFunc.mockResolvedValue({ id: 1, name: '测试数据' })

  const { data, loading, execute } = useRequest(mockApiFunc)

  const result = await execute()

  expect(result).toEqual({ id: 1, name: '测试数据' })
  expect(data.value).toEqual({ id: 1, name: '测试数据' })
})
```

## 🐛 调试测试

### 运行单个测试
```bash
npm test -- -t "should login successfully"
```

### 显示详细输出
```bash
npm test -- --reporter=verbose
```

### 调试模式
```bash
node --inspect-brk node_modules/.bin/vitest --run
```

## ⚠️ 注意事项

1. **不需要后端运行**: 所有测试使用mock数据，无需启动后端服务
2. **不需要浏览器**: 测试在Node.js环境中运行
3. **快速反馈**: 完整测试套件在几秒钟内完成

## 📈 下一步

如需添加更多测试：
1. 在`__tests__`目录创建新的测试文件
2. 使用`describe`分组测试
3. 使用`it`编写单个测试用例
4. Mock所有外部依赖
5. 验证函数调用和返回值

测试文件命名规范：`*.test.js` 或 `*.spec.js`
