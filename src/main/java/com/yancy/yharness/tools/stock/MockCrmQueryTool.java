package com.yancy.yharness.tools.stock;

import com.yancy.yharness.tools.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockCrmQueryTool implements Tool {
    @Override
    public String getName() {
        return "queryCustomerProfile";
    }

    @Override
    public String getDescription() {
        return "查询客户CRM画像信息，包括行业、等级、偏好、最近联系时间等";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition(getName(), getDescription());
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> userIdProp = new HashMap<>();
        userIdProp.put("type", "string");
        userIdProp.put("description", "商家/客户ID");
        props.put("userId", userIdProp);
        schema.put("properties", props);
        schema.put("required", new String[]{"userId"});
        def.setInputSchema(schema);
        def.setSideEffectLevel("READ_ONLY");
        def.setDomain("crm");
        def.setTags(java.util.Arrays.asList("query-basic", "crm"));
        def.setTimeoutMs(3000);
        def.setIdempotent(true);
        return def;
    }

    @Override
    public ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments) {
        String userId = (String) arguments.getOrDefault("userId", "unknown");
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("name", "Demo Store Co., Ltd.");
        profile.put("industry", "Retail");
        profile.put("tier", "Premium");
        profile.put("language", "en");
        profile.put("tenureMonths", 24);
        profile.put("lastContactAt", "2026-05-20T10:30:00");
        profile.put("engagementScore", 85);
        profile.put("totalSpend", 125000.00);
        profile.put("leadStage", "WARM_LEAD");
        profile.put("interestTags", java.util.Arrays.asList("seasonal-promotion", "new-arrivals"));

        return ToolResult.success(profile,
                "客户 " + userId + " 是零售行业高级会员，已合作24个月，最近联系日期为3天前，参与度为85分。");
    }
}