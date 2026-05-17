
package com.example.yharness.tools.sales;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.CustomerProfile;
import com.example.yharness.context.ToolDefinition;
import com.example.yharness.context.ToolParameter;
import com.example.yharness.tools.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomerAnalyzer implements Tool {

    private final ToolDefinition definition;

    public CustomerAnalyzer() {
        this.definition = buildDefinition();
    }

    private ToolDefinition buildDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName("analyzeCustomer");
        def.setDescription("分析客户画像，生成针对性的销售建议");
        
        ToolParameter customerInfoParam = new ToolParameter("customerInfo", "string", "客户信息描述", true);
        
        def.addParameter(customerInfoParam);
        
        return def;
    }

    @Override
    public String getName() {
        return "analyzeCustomer";
    }

    @Override
    public String getDescription() {
        return "分析客户画像，生成针对性的销售建议";
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String customerInfo = (String) arguments.get("customerInfo");
        
        CustomerProfile profile = parseCustomerInfo(customerInfo);
        context.getBusinessState().setCustomerProfile(profile);
        
        return generateAnalysis(profile);
    }

    private CustomerProfile parseCustomerInfo(String customerInfo) {
        CustomerProfile profile = new CustomerProfile();
        
        if (customerInfo.contains("公司") || customerInfo.contains("企业")) {
            int idx = customerInfo.indexOf("公司");
            if (idx > 0) {
                String company = customerInfo.substring(0, idx).trim();
                profile.setCompany(company);
            }
        }
        
        if (customerInfo.contains("预算")) {
            profile.setBudgetRange("需要进一步确认");
        }
        
        if (customerInfo.contains("痛点") || customerInfo.contains("问题") || customerInfo.contains("挑战")) {
            profile.addPainPoint("待确认具体痛点");
        }
        
        return profile;
    }

    private String generateAnalysis(CustomerProfile profile) {
        StringBuilder result = new StringBuilder();
        result.append("【客户画像分析】\n");
        
        if (profile.getCompany() != null && !profile.getCompany().isEmpty()) {
            result.append("公司名称: ").append(profile.getCompany()).append("\n");
        }
        
        result.append("当前销售阶段: ").append("初步接触").append("\n");
        
        if (!profile.getPainPoints().isEmpty()) {
            result.append("已识别痛点: ").append(String.join(", ", profile.getPainPoints())).append("\n");
        }
        
        result.append("\n【销售建议】\n");
        result.append("1. 建议进一步了解客户具体业务需求\n");
        result.append("2. 根据客户行业特点定制解决方案\n");
        result.append("3. 准备相关成功案例进行展示\n");
        result.append("4. 了解客户决策链和预算情况\n");
        
        return result.toString();
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }
}
