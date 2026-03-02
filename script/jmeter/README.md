# Campus Platform JMeter 压力测试

## 概述

本目录包含用于对校园平台进行压力测试的 JMeter 测试计划。

## 文件说明

| 文件 | 说明 |
|------|------|
| `campus-platform-test.jmx` | JMeter 测试计划文件 |
| `run-test.sh` | Linux/Mac 运行脚本 |
| `run-test-docker.bat` | Windows Docker 运行脚本 |

## 测试接口

| 编号 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 01 | `/api/auth/login` | POST | 用户登录获取Token |
| 02 | `/api/boards` | GET | 论坛板块列表 |
| 03 | `/api/posts` | GET | 帖子列表(分页) |
| 04 | `/api/posts/{id}` | GET | 帖子详情 |
| 05 | `/api/items` | GET | 二手商品列表(分页) |

## 快速开始

### 方式一: 本地安装 JMeter

1. 下载 JMeter: https://jmeter.apache.org/download.cgi
2. 解压并运行:
```bash
cd script/jmeter
./run-test.sh 10 5 60 localhost:8080
```

参数说明: `./run-test.sh [并发数] [启动时间(秒)] [持续时间(秒)] [目标地址]`

### 方式二: 使用 Docker (推荐)

**Windows:**
```cmd
script\jmeter\run-test-docker.bat 10 5 60 host.docker.internal:8080
```

**Linux/Mac:**
```bash
docker run --rm \
    -v $(pwd)/script/jmeter:/workspace \
    -v $(pwd)/script/jmeter/reports:/reports \
    justb4/jmeter:latest \
    -n \
    -t /workspace/campus-platform-test.jmx \
    -JTHREADS=10 \
    -JRAMP_UP=5 \
    -JDURATION=60 \
    -JBASE_URL=host.docker.internal:8080 \
    -l /reports/results.jtl \
    -e \
    -o /reports/html
```

## 参数说明

| 参数 | 默认值 | 说明 |
|------|--------|------|
| THREADS | 10 | 并发线程数 |
| RAMP_UP | 5 | 启动时间(秒)，逐步增加线程 |
| DURATION | 60 | 持续时间(秒) |
| BASE_URL | localhost:8080 | 目标服务器地址 |

## 报告输出

测试完成后会在 `reports/` 目录下生成:

- `results.jtl` - 原始结果数据
- `html/index.html` - HTML可视化报告

用浏览器打开 `html/index.html` 查看详细报告。

## 测试场景配置

编辑 `campus-platform-test.jmx` 文件可以:
- 添加/删除测试接口
- 修改请求参数
- 添加认证Token
- 配置断言

建议测试步骤:
1. 先用小并发(10线程)测试
2. 确认系统正常后逐步增加并发
3. 观察响应时间和错误率
