#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YHarness Agent API 测试脚本
测试股票咨询Agent框架的核心接口功能
"""

import requests
import json
import time
import sys

# 配置
BASE_URL = "http://localhost:8080/api/agent"
TIMEOUT = 30

# ANSI颜色代码
class Color:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    END = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def print_divider(title=None, style="="):
    """打印分隔线"""
    if title:
        line = style * 70
        print(f"\n{line}")
        print(f"  {Color.BOLD}{title}{Color.END}")
        print(line)
    else:
        print(style * 70)

def print_json(data, indent=2, prefix=""):
    """递归打印JSON，美化嵌套结构"""
    if isinstance(data, dict):
        print(f"{prefix}{{")
        keys = list(data.keys())
        for i, key in enumerate(keys):
            value = data[key]
            comma = "," if i < len(keys) - 1 else ""
            print(f"{prefix}  \"{key}\": ", end="")
            if isinstance(value, dict):
                print()
                print_json(value, indent, prefix + "  ")
                print(f"{prefix}{comma}")
            elif isinstance(value, list):
                print()
                print_json(value, indent, prefix + "  ")
                print(f"{prefix}{comma}")
            elif isinstance(value, str):
                try:
                    nested_json = json.loads(value)
                    print()
                    print_json(nested_json, indent, prefix + "  ")
                    print(f"{prefix}{comma}")
                except (json.JSONDecodeError, ValueError):
                    print(f'"{value}"{comma}')
            else:
                print(f"{json.dumps(value)}{comma}")
        print(f"{prefix}}}")
    elif isinstance(data, list):
        print(f"{prefix}[")
        for i, item in enumerate(data):
            comma = "," if i < len(data) - 1 else ""
            if isinstance(item, dict):
                print_json(item, indent, prefix + "  ")
                print(f"{prefix}{comma}")
            elif isinstance(item, str):
                try:
                    nested_json = json.loads(item)
                    print_json(nested_json, indent, prefix + "  ")
                    print(f"{prefix}{comma}")
                except (json.JSONDecodeError, ValueError):
                    print(f'{prefix}  "{item}"{comma}')
            else:
                print(f"{prefix}  {json.dumps(item)}{comma}")
        print(f"{prefix}]")
    else:
        print(f"{prefix}{json.dumps(data)}")

def print_response(response):
    """格式化打印响应"""
    print(f"\n{Color.BLUE}┌─────────────────────────────────────────────────────────────────────┐{Color.END}")
    print(f"{Color.BLUE}│ 状态码: {response.status_code:3d} │ 响应时间: {response.elapsed.total_seconds():.2f}s{Color.END}")
    print(f"{Color.BLUE}└─────────────────────────────────────────────────────────────────────┘{Color.END}")
    print(f"{Color.BOLD}响应内容:{Color.END}")
    try:
        data = response.json()
        print_json(data)
    except json.JSONDecodeError:
        print(f"  {response.text}")
    except Exception as e:
        print(f"  {Color.RED}解析响应失败: {e}{Color.END}")
        print(f"  原始响应: {response.text}")

def test_health():
    """测试健康检查接口"""
    print(f"\n{Color.GREEN}[测试1/8]{Color.END} 健康检查接口")
    try:
        response = requests.get(f"{BASE_URL}/health", timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_new_conversation():
    """测试新建对话"""
    print(f"\n{Color.GREEN}[测试2/8]{Color.END} 新建对话")
    try:
        payload = {
            "message": "你好，我想咨询一下股票投资"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                conv_id = data.get("conversationId")
                print(f"  {Color.YELLOW}✓ 对话ID: {conv_id}{Color.END}")
                return True, conv_id
        return False, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_continue_conversation(conversation_id):
    """测试继续对话"""
    print(f"\n{Color.GREEN}[测试3/8]{Color.END} 继续对话")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "我是一名稳健型投资者，有什么好的投资建议吗？"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_tool_call(conversation_id):
    """测试工具调用功能 - 股票行情"""
    print(f"\n{Color.GREEN}[测试4/8]{Color.END} 工具调用 - 查询股票行情")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "请帮我查询贵州茅台600519的实时行情"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_stock_analysis(conversation_id):
    """测试股票分析功能"""
    print(f"\n{Color.GREEN}[测试5/8]{Color.END} 股票分析")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "帮我分析一下600519这只股票，做个综合分析"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_get_context(conversation_id):
    """测试获取上下文信息"""
    print(f"\n{Color.GREEN}[测试6/8]{Color.END} 获取上下文信息")
    try:
        response = requests.get(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_clear_context(conversation_id):
    """测试清除上下文"""
    print(f"\n{Color.GREEN}[测试7/8]{Color.END} 清除上下文")
    try:
        response = requests.delete(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print_response(response)
        
        get_response = requests.get(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print(f"\n  {Color.BOLD}验证上下文是否已删除:{Color.END}")
        print(f"    状态码: {get_response.status_code}")
        if get_response.status_code == 200:
            data = get_response.json()
            status = "已删除" if not data.get("success") else "未删除"
            status_color = Color.GREEN if status == "已删除" else Color.RED
            print(f"    结果: {status_color}{status}{Color.END}")
        
        return response.status_code == 200, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def test_risk_assessment():
    """测试风险评估功能"""
    print(f"\n{Color.GREEN}[测试8/8]{Color.END} 投资者风险评估")
    try:
        payload = {
            "message": "我今年35岁，年收入50万，有5年投资经验，风险承受能力中等，目标是资产增值"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        
        if response.status_code == 200:
            data = response.json()
            return data.get("success", False), None
        return False, None
    except Exception as e:
        print(f"  {Color.RED}请求失败: {e}{Color.END}")
        return False, None

def run_all_tests():
    """运行所有测试"""
    print_divider(f"{Color.HEADER}YHarness 股票咨询Agent API 测试套件{Color.END}", "═")
    
    results = []
    conversation_id = None
    
    # 测试1: 健康检查
    success, _ = test_health()
    results.append(("健康检查", success))
    
    # 测试2: 新建对话
    success, conv_id = test_new_conversation()
    conversation_id = conv_id
    results.append(("新建对话", success))
    
    if conversation_id:
        # 测试3: 继续对话
        success, _ = test_continue_conversation(conversation_id)
        results.append(("继续对话", success))
        
        # 测试4: 工具调用
        success, _ = test_tool_call(conversation_id)
        results.append(("股票行情查询", success))
        
        # 测试5: 股票分析
        success, _ = test_stock_analysis(conversation_id)
        results.append(("股票分析", success))
        
        # 测试6: 获取上下文
        success, _ = test_get_context(conversation_id)
        results.append(("获取上下文", success))
        
        # 测试7: 清除上下文
        success, _ = test_clear_context(conversation_id)
        results.append(("清除上下文", success))
    
    # 测试8: 风险评估
    success, _ = test_risk_assessment()
    results.append(("风险评估", success))
    
    # 输出测试报告
    print_divider(f"{Color.BOLD}测试报告{Color.END}", "═")
    
    passed = sum(1 for _, success in results if success)
    total = len(results)
    
    print(f"\n{Color.BOLD}测试结果汇总:{Color.END}")
    print("─" * 40)
    
    for i, (test_name, success) in enumerate(results, 1):
        status_icon = f"{Color.GREEN}✓{Color.END}" if success else f"{Color.RED}✗{Color.END}"
        status_text = f"{Color.GREEN}通过{Color.END}" if success else f"{Color.RED}失败{Color.END}"
        print(f"  [{i}] {status_icon} {test_name:<12} - {status_text}")
    
    print("\n" + "─" * 40)
    print(f"  总计: {Color.BOLD}{passed}/{total}{Color.END} 测试通过")
    
    # 进度条
    progress = int((passed / total) * 20)
    progress_bar = f"{'█' * progress}{'░' * (20 - progress)}"
    print(f"  进度: [{Color.BLUE}{progress_bar}{Color.END}] {int((passed / total) * 100)}%")
    
    if passed == total:
        print(f"\n{Color.GREEN}🎉 所有测试通过！{Color.END}")
        return 0
    else:
        print(f"\n{Color.YELLOW}⚠️ 部分测试失败，请检查日志{Color.END}")
        return 1

if __name__ == "__main__":
    try:
        sys.exit(run_all_tests())
    except KeyboardInterrupt:
        print(f"\n{Color.YELLOW}测试被用户中断{Color.END}")
        sys.exit(1)
    except Exception as e:
        print(f"\n{Color.RED}测试执行异常: {e}{Color.END}")
        sys.exit(1)
