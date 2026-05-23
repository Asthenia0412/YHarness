package com.yancy.yharness.tools.stock;

import com.yancy.yharness.tools.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockKnowledgeBaseTool implements Tool {
    @Override
    public String getName() {
        return "searchKnowledge";
    }

    @Override
    public String getDescription() {
        return "搜索知识库，查询FAQ、产品文档、政策说明等知识信息";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition(getName(), getDescription());
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "搜索关键词");
        props.put("query", queryProp);
        Map<String, Object> categoryProp = new HashMap<>();
        categoryProp.put("type", "string");
        categoryProp.put("description", "知识分类: faq / policy / product");
        props.put("category", categoryProp);
        schema.put("properties", props);
        schema.put("required", new String[]{"query"});
        def.setInputSchema(schema);
        def.setSideEffectLevel("READ_ONLY");
        def.setDomain("knowledge");
        def.setTags(java.util.Arrays.asList("knowledge-retrieval", "faq"));
        def.setTimeoutMs(5000);
        def.setIdempotent(true);
        return def;
    }

    @Override
    public ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments) {
        String query = (String) arguments.getOrDefault("query", "");
        String category = (String) arguments.getOrDefault("category", "faq");

        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("category", category);
        result.put("highRelevance", java.util.Arrays.asList(
                "标准优惠方案是首次投放返现10%，适用于所有新客户",
                "美妆行业客户可申请额外素材补贴，上限$2,000",
                "广告投放最低预算为$500/月"
        ));
        result.put("lowRelevance", java.util.Arrays.asList(
                "2025年Q4的促销活动已经结束，请关注新活动通知"
        ));

        return ToolResult.success(result,
                "找到3条相关知识：" + query + "相关标准方案是首次投放返现10%。");
    }
}