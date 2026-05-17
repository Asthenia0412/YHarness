
package com.example.yharness.tools.sales;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.ToolDefinition;
import com.example.yharness.context.ToolParameter;
import com.example.yharness.tools.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductQueryTool implements Tool {

    private final ToolDefinition definition;
    private final Map<String, ProductInfo> productDatabase;

    public ProductQueryTool() {
        this.definition = buildDefinition();
        this.productDatabase = buildProductDatabase();
    }

    private ToolDefinition buildDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName("queryProductInfo");
        def.setDescription("查询产品详细信息");
        
        ToolParameter productIdParam = new ToolParameter("productId", "string", "产品ID", true);
        
        def.addParameter(productIdParam);
        
        return def;
    }

    private Map<String, ProductInfo> buildProductDatabase() {
        Map<String, ProductInfo> db = new HashMap<>();
        
        db.put("CRM-PRO", new ProductInfo(
            "CRM-PRO",
            "企业级客户关系管理系统",
            "专为大型企业设计的全功能CRM解决方案，支持销售自动化、客户服务、营销自动化等核心功能",
            new String[]{"销售自动化", "客户服务", "营销自动化", "数据分析", "移动端支持"},
            "¥50,000/年起",
            "30天免费试用"
        ));
        
        db.put("ERP-SMART", new ProductInfo(
            "ERP-SMART",
            "智能企业资源规划系统",
            "集成财务、采购、库存、生产管理的一站式ERP解决方案",
            new String[]{"财务管理", "采购管理", "库存管理", "生产计划", "供应链协同"},
            "¥80,000/年起",
            "15天免费试用"
        ));
        
        db.put("BI-ANALYST", new ProductInfo(
            "BI-ANALYST",
            "商业智能分析平台",
            "强大的数据可视化和分析工具，帮助企业快速洞察业务数据",
            new String[]{"数据可视化", "自助分析", "预测分析", "实时报表", "多数据源接入"},
            "¥30,000/年起",
            "21天免费试用"
        ));
        
        return db;
    }

    @Override
    public String getName() {
        return "queryProductInfo";
    }

    @Override
    public String getDescription() {
        return "查询产品详细信息";
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String productId = (String) arguments.get("productId");
        
        ProductInfo product = productDatabase.get(productId);
        if (product == null) {
            return "未找到产品: " + productId;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("【产品信息】\n");
        result.append("产品ID: ").append(product.getId()).append("\n");
        result.append("产品名称: ").append(product.getName()).append("\n");
        result.append("产品描述: ").append(product.getDescription()).append("\n");
        result.append("核心功能: ").append(String.join(", ", product.getFeatures())).append("\n");
        result.append("价格: ").append(product.getPrice()).append("\n");
        result.append("试用政策: ").append(product.getTrialPolicy()).append("\n");
        
        return result.toString();
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }

    private static class ProductInfo {
        private String id;
        private String name;
        private String description;
        private String[] features;
        private String price;
        private String trialPolicy;

        public ProductInfo(String id, String name, String description, String[] features, String price, String trialPolicy) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.features = features;
            this.price = price;
            this.trialPolicy = trialPolicy;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String[] getFeatures() { return features; }
        public String getPrice() { return price; }
        public String getTrialPolicy() { return trialPolicy; }
    }
}
