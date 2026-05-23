package com.yancy.yharness.tools.stock;

import com.yancy.yharness.tools.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockAdPerformanceTool implements Tool {
    @Override
    public String getName() {
        return "queryAdPerformance";
    }

    @Override
    public String getDescription() {
        return "查询广告投放效果数据，包括展示量、点击量、花费、转化率等指标";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition(getName(), getDescription());
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> accountIdProp = new HashMap<>();
        accountIdProp.put("type", "string");
        accountIdProp.put("description", "广告账户ID");
        props.put("accountId", accountIdProp);
        Map<String, Object> dateRangeProp = new HashMap<>();
        dateRangeProp.put("type", "string");
        dateRangeProp.put("description", "日期范围: last7days / last30days / yesterday");
        props.put("dateRange", dateRangeProp);
        schema.put("properties", props);
        schema.put("required", new String[]{"accountId"});
        def.setInputSchema(schema);
        def.setSideEffectLevel("READ_ONLY");
        def.setDomain("advertising");
        def.setTags(java.util.Arrays.asList("query-basic", "advertising"));
        def.setTimeoutMs(5000);
        def.setIdempotent(true);
        return def;
    }

    @Override
    public ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments) {
        String accountId = (String) arguments.getOrDefault("accountId", "default");
        String dateRange = (String) arguments.getOrDefault("dateRange", "last7days");

        Map<String, Object> performance = new HashMap<>();
        performance.put("accountId", accountId);
        performance.put("dateRange", dateRange);
        performance.put("impressions", 45230);
        performance.put("clicks", 1234);
        performance.put("ctr", 2.73);
        performance.put("spend", 1250.00);
        performance.put("conversions", 89);
        performance.put("conversionRate", 7.21);
        performance.put("cpa", 14.04);
        performance.put("roi", 3.2);
        performance.put("status", "ACTIVE");

        return ToolResult.success(performance,
                "广告账户 " + accountId + " 近7天展示45,230次，点击1,234次，CTR 2.73%，花费$1,250.00，转化率7.21%。");
    }
}