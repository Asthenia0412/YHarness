package com.yancy.yharness.tools.stock;

import com.yancy.yharness.tools.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockPromotionTool implements Tool {
    @Override
    public String getName() {
        return "queryPromotions";
    }

    @Override
    public String getDescription() {
        return "查询当前可用的优惠政策、折扣方案和补贴活动";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition(getName(), getDescription());
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> industryProp = new HashMap<>();
        industryProp.put("type", "string");
        industryProp.put("description", "行业类型，如 retail / beauty / education");
        props.put("industry", industryProp);
        schema.put("properties", props);
        def.setInputSchema(schema);
        def.setSideEffectLevel("READ_ONLY");
        def.setDomain("promotion");
        def.setTags(java.util.Arrays.asList("query-basic", "promotion"));
        def.setTimeoutMs(3000);
        def.setIdempotent(true);
        return def;
    }

    @Override
    public ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments) {
        String industry = (String) arguments.getOrDefault("industry", "retail");

        Map<String, Object> promotions = new HashMap<>();
        promotions.put("industry", industry);
        promotions.put("availablePromotions", java.util.Arrays.asList(
                "新用户首次投放返现10%",
                "季度大促额外曝光补贴$500",
                "Referral奖励计划-推荐新客户得$200"
        ));
        promotions.put("activeCampaigns", java.util.Arrays.asList(
                "Summer Sale 2026 - 额外15%曝光量",
                "周末限时折扣 - CPC降低20%"
        ));

        return ToolResult.success(promotions,
                industry + "行业当前有3个可用优惠方案：新用户返现10%、季度补贴$500、推荐奖励$200。");
    }
}