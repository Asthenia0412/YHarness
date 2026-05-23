package com.yancy.yharness.tools.stock;

import com.yancy.yharness.tools.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MockInventoryTool implements Tool {
    @Override
    public String getName() {
        return "queryInventory";
    }

    @Override
    public String getDescription() {
        return "查询商品库存信息，包括各门店库存数量、SKU详情等";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition(getName(), getDescription());
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> skuProp = new HashMap<>();
        skuProp.put("type", "string");
        skuProp.put("description", "商品SKU编码");
        props.put("sku", skuProp);
        Map<String, Object> storeProp = new HashMap<>();
        storeProp.put("type", "string");
        storeProp.put("description", "门店ID（可选）");
        props.put("storeId", storeProp);
        schema.put("properties", props);
        schema.put("required", new String[]{"sku"});
        def.setInputSchema(schema);
        def.setSideEffectLevel("READ_ONLY");
        def.setDomain("inventory");
        def.setTags(java.util.Arrays.asList("query-basic", "inventory"));
        def.setTimeoutMs(3000);
        def.setIdempotent(true);
        return def;
    }

    @Override
    public ToolResult execute(ToolExecutionContext context, Map<String, Object> arguments) {
        String sku = (String) arguments.getOrDefault("sku", "SKU-001");

        Map<String, Object> inventory = new HashMap<>();
        inventory.put("sku", sku);
        inventory.put("productName", "Premium Widget Pro");
        inventory.put("totalStock", 150);
        inventory.put("stores", java.util.Arrays.asList(
                new HashMap<String, Object>() {{ put("storeId", "S001"); put("name", "北京朝阳店"); put("quantity", 45); }},
                new HashMap<String, Object>() {{ put("storeId", "S002"); put("name", "上海浦东店"); put("quantity", 62); }},
                new HashMap<String, Object>() {{ put("storeId", "S003"); put("name", "广州天河店"); put("quantity", 43); }}
        ));
        inventory.put("price", 299.99);
        inventory.put("status", "IN_STOCK");

        return ToolResult.success(inventory,
                "商品 " + sku + " (Premium Widget Pro) 总库存150件，北京朝阳店45件、上海浦东店62件、广州天河店43件，单价$299.99。");
    }
}