#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YHarness Agent API 测试脚本
测试销售Agent框架的核心接口功能
"""

import requests
import json
import time
import sys

# 配置
BASE_URL = "http://localhost:8080/api/agent"
TIMEOUT = 30

def print_response(response):
    """格式化打印响应"""
    print(f"\n{'='*60}")
    print(f"状态码: {response.status_code}")
    print(f"响应时间: {response.elapsed.total_seconds():.2f}s")
    print("响应内容:")
    try:
        data = response.json()
        print(json.dumps(data, ensure_ascii=False, indent=2))
    except:
        print(response.text)
    print(f"{'='*60}\n")

def test_health():
    """测试健康检查接口"""
    print("\n[测试1] 健康检查接口")
    try:
        response = requests.get(f"{BASE_URL}/health", timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_new_conversation():
    """测试新建对话"""
    print("\n[测试2] 新建对话")
    try:
        payload = {
            "message": "你好，我想了解一下CRM产品"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                return data.get("conversationId")
        return None
    except Exception as e:
        print(f"请求失败: {e}")
        return None

def test_continue_conversation(conversation_id):
    """测试继续对话"""
    print("\n[测试3] 继续对话")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "这个产品有哪些核心功能？"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_tool_call(conversation_id):
    """测试工具调用功能"""
    print("\n[测试4] 测试工具调用 - 查询产品信息")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "请帮我查询CRM-PRO产品的详细信息"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_generate_sales_script(conversation_id):
    """测试生成销售话术"""
    print("\n[测试5] 测试生成销售话术")
    try:
        payload = {
            "conversationId": conversation_id,
            "message": "帮我生成一个初次接触的销售话术，产品是CRM-PRO"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_get_context(conversation_id):
    """测试获取上下文信息"""
    print("\n[测试6] 获取上下文信息")
    try:
        response = requests.get(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_clear_context(conversation_id):
    """测试清除上下文"""
    print("\n[测试7] 清除上下文")
    try:
        response = requests.delete(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print_response(response)
        
        # 验证上下文已删除
        get_response = requests.get(f"{BASE_URL}/context/{conversation_id}", timeout=TIMEOUT)
        print("\n验证上下文是否已删除:")
        print(f"状态码: {get_response.status_code}")
        if get_response.status_code == 200:
            data = get_response.json()
            print(f"结果: {'已删除' if not data.get('success') else '未删除'}")
        
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_customer_analysis():
    """测试客户分析功能"""
    print("\n[测试8] 测试客户分析功能")
    try:
        payload = {
            "message": "分析一下这个客户：张三，来自腾讯公司，担任技术总监，预算充足，主要痛点是客户管理效率低"
        }
        response = requests.post(f"{BASE_URL}/chat", json=payload, timeout=TIMEOUT)
        print_response(response)
        return response.status_code == 200
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def run_all_tests():
    """运行所有测试"""
    print("="*60)
    print("YHarness Agent API 测试套件")
    print("="*60)
    
    results = []
    
    # 测试1: 健康检查
    results.append(("健康检查", test_health()))
    
    # 测试2: 新建对话
    conversation_id = test_new_conversation()
    results.append(("新建对话", conversation_id is not None))
    
    if conversation_id:
        # 测试3: 继续对话
        results.append(("继续对话", test_continue_conversation(conversation_id)))
        
        # 测试4: 工具调用
        results.append(("工具调用", test_tool_call(conversation_id)))
        
        # 测试5: 生成销售话术
        results.append(("生成销售话术", test_generate_sales_script(conversation_id)))
        
        # 测试6: 获取上下文
        results.append(("获取上下文", test_get_context(conversation_id)))
        
        # 测试7: 清除上下文
        results.append(("清除上下文", test_clear_context(conversation_id)))
    
    # 测试8: 客户分析
    results.append(("客户分析", test_customer_analysis()))
    
    # 输出测试报告
    print("\n" + "="*60)
    print("测试报告")
    print("="*60)
    
    passed = sum(1 for _, success in results if success)
    total = len(results)
    
    for test_name, success in results:
        status = "✓ 通过" if success else "✗ 失败"
        print(f"{status} - {test_name}")
    
    print(f"\n总计: {passed}/{total} 测试通过")
    
    if passed == total:
        print("\n🎉 所有测试通过！")
        return 0
    else:
        print("\n⚠️ 部分测试失败，请检查日志")
        return 1

if __name__ == "__main__":
    try:
        sys.exit(run_all_tests())
    except KeyboardInterrupt:
        print("\n测试被用户中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n测试执行异常: {e}")
        sys.exit(1)
