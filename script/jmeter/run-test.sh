#!/bin/bash
# JMeter 压力测试运行脚本

# 配置参数
THREADS=${1:-10}        # 并发线程数，默认10
RAMP_UP=${2:-5}         # 启动时间(秒)，默认5
DURATION=${3:-60}       # 持续时间(秒)，默认60
BASE_URL=${4:-localhost:8080}

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JMX_FILE="$SCRIPT_DIR/campus-platform-test.jmx"
REPORT_DIR="$SCRIPT_DIR/reports/$(date +%Y%m%d_%H%M%S)"

# 创建报告目录
mkdir -p "$REPORT_DIR"

echo "=========================================="
echo "Campus Platform JMeter 压力测试"
echo "=========================================="
echo "并发线程数: $THREADS"
echo "启动时间: ${RAMP_UP}秒"
echo "持续时间: ${DURATION}秒"
echo "目标地址: $BASE_URL"
echo "报告目录: $REPORT_DIR"
echo "=========================================="

# 检查JMeter是否安装
if ! command -v jmeter &> /dev/null; then
    echo "错误: JMeter未安装"
    echo "请先安装JMeter: https://jmeter.apache.org/"
    echo "或使用Docker运行:"
    echo "  docker run -v \$(pwd):/workspace justb4/jmeter:latest -n -t /workspace/script/jmeter/campus-platform-test.jmx"
    exit 1
fi

# 运行测试
jmeter -n \
    -t "$JMX_FILE" \
    -JTHREADS="$THREADS" \
    -JRAMP_UP="$RAMP_UP" \
    -JDURATION="$DURATION" \
    -JBASE_URL="$BASE_URL" \
    -JREPORT_DIR="$REPORT_DIR" \
    -l "$REPORT_DIR/results.jtl" \
    -e \
    -o "$REPORT_DIR/html"

echo ""
echo "=========================================="
echo "测试完成!"
echo "=========================================="
echo "HTML报告: file://$REPORT_DIR/html/index.html"
echo "详细结果: file://$REPORT_DIR/results.jtl"
echo "=========================================="
