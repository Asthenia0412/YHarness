
package com.example.yharness.tools.sales;

import com.example.yharness.context.AgentContext;
import com.example.yharness.context.ToolDefinition;
import com.example.yharness.context.ToolParameter;
import com.example.yharness.tools.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SalesScriptGenerator implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(SalesScriptGenerator.class);
    
    private final ToolDefinition definition;

    public SalesScriptGenerator() {
        this.definition = buildDefinition();
    }

    private ToolDefinition buildDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName("generateSalesScript");
        def.setDescription("根据产品信息和客户画像生成专业的销售话术");
        
        ToolParameter productIdParam = new ToolParameter("productId", "string", "产品ID", true);
        ToolParameter customerProfileParam = new ToolParameter("customerProfile", "string", "客户画像描述", false);
        ToolParameter scenarioParam = new ToolParameter("scenario", "string", "销售场景：初次接触/需求挖掘/方案演示/跟进", false);
        
        def.addParameter(productIdParam);
        def.addParameter(customerProfileParam);
        def.addParameter(scenarioParam);
        
        return def;
    }

    @Override
    public String getName() {
        return "generateSalesScript";
    }

    @Override
    public String getDescription() {
        return "根据产品信息和客户画像生成专业的销售话术";
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String productId = (String) arguments.get("productId");
        String customerProfile = (String) arguments.get("customerProfile");
        String scenario = (String) arguments.get("scenario");
        
        if (scenario == null) {
            scenario = "初次接触";
        }

        String script = generateScript(productId, customerProfile, scenario);
        
        context.getBusinessState().setNextAction("使用生成的销售话术与客户沟通");
        
        return script;
    }

    private String generateScript(String productId, String customerProfile, String scenario) {
        StringBuilder script = new StringBuilder();
        
        script.append("【销售话术 - ").append(scenario).append("】\n\n");
        script.append("尊敬的客户，您好！\n\n");
        
        switch (scenario) {
            case "初次接触":
                script.append("我是[公司名称]的销售顾问，很高兴能与您取得联系。")
                      .append("\n最近我们关注到贵公司在[行业领域]的业务发展，")
                      .append("\n我们的").append(productId).append("产品可以帮助您解决")
                      .append("\n[具体痛点]方面的挑战，不知您是否方便沟通几分钟？");
                break;
                
            case "需求挖掘":
                script.append("感谢您抽出时间与我交流。")
                      .append("\n在介绍我们的产品之前，我想了解一下：")
                      .append("\n1. 您目前在[相关业务]方面遇到了哪些挑战？")
                      .append("\n2. 您对解决方案有哪些期望和要求？")
                      .append("\n3. 您在选型时最看重哪些方面？");
                break;
                
            case "方案演示":
                script.append("根据我们之前的沟通，我为您准备了针对性的解决方案。")
                      .append("\n我们的").append(productId).append("产品具备以下核心优势：")
                      .append("\n• 功能优势1：[具体优势]")
                      .append("\n• 功能优势2：[具体优势]")
                      .append("\n• 成功案例：[客户案例]")
                      .append("\n\n您觉得这个方案是否符合您的需求？");
                break;
                
            case "跟进":
                script.append("您好，我是[公司名称]的[姓名]，之前我们沟通过").append(productId).append("产品。")
                      .append("\n想了解一下您这边是否有新的进展或需要进一步的信息？")
                      .append("\n如果您有任何疑问，随时欢迎联系我。");
                break;
                
            default:
                script.append("您好！关于").append(productId).append("产品，")
                      .append("\n我想与您分享一些可能对您有帮助的信息。")
                      .append("\n方便的话，我们可以安排一个简短的沟通吗？");
        }
        
        script.append("\n\n如有任何问题，请随时告诉我！");
        
        return script.toString();
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }
}
