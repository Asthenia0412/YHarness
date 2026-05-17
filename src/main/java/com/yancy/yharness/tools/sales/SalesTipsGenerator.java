
package com.yancy.yharness.tools.sales;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.context.ToolDefinition;
import com.yancy.yharness.context.ToolParameter;
import com.yancy.yharness.tools.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SalesTipsGenerator implements Tool {

    private final ToolDefinition definition;

    public SalesTipsGenerator() {
        this.definition = buildDefinition();
    }

    private ToolDefinition buildDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName("getSalesTips");
        def.setDescription("根据销售场景获取专业的销售建议和技巧");
        
        ToolParameter scenarioParam = new ToolParameter("scenario", "string", "销售场景", true);
        ToolParameter productIdParam = new ToolParameter("productId", "string", "产品ID", false);
        
        def.addParameter(scenarioParam);
        def.addParameter(productIdParam);
        
        return def;
    }

    @Override
    public String getName() {
        return "getSalesTips";
    }

    @Override
    public String getDescription() {
        return "根据销售场景获取专业的销售建议和技巧";
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String scenario = (String) arguments.get("scenario");
        String productId = (String) arguments.get("productId");
        
        return generateTips(scenario, productId);
    }

    private String generateTips(String scenario, String productId) {
        StringBuilder tips = new StringBuilder();
        tips.append("【销售建议 - ").append(scenario).append("】\n\n");
        
        switch (scenario) {
            case "处理价格异议":
                tips.append("• 强调产品价值而非单纯比较价格\n");
                tips.append("• 提供分期付款或优惠方案\n");
                tips.append("• 对比总拥有成本(TCO)\n");
                tips.append("• 展示投资回报率(ROI)数据\n");
                tips.append("• 提供限时优惠创造紧迫感\n");
                break;
                
            case "应对竞争对手":
                tips.append("• 深入了解竞争对手的优缺点\n");
                tips.append("• 突出自身差异化优势\n");
                tips.append("• 避免直接贬低竞争对手\n");
                tips.append("• 用客户案例证明实力\n");
                tips.append("• 提供更优质的服务承诺\n");
                break;
                
            case "需求挖掘":
                tips.append("• 使用开放式问题引导客户表达\n");
                tips.append("• 倾听并确认客户需求\n");
                tips.append("• 挖掘潜在需求和隐藏痛点\n");
                tips.append("• 使用SPIN提问技巧\n");
                tips.append("• 记录关键信息供后续跟进\n");
                break;
                
            case "方案演示":
                tips.append("• 提前了解客户关注点\n");
                tips.append("• 保持演示简洁明了\n");
                tips.append("• 互动式演示增加参与感\n");
                tips.append("• 准备FAQ应对疑问\n");
                tips.append("• 演示后及时总结核心价值\n");
                break;
                
            case "跟进策略":
                tips.append("• 设定明确的跟进时间节点\n");
                tips.append("• 提供有价值的跟进内容\n");
                tips.append("• 多种渠道组合跟进\n");
                tips.append("• 保持适度的跟进频率\n");
                tips.append("• 记录每次跟进结果\n");
                break;
                
            default:
                tips.append("• 保持专业形象和态度\n");
                tips.append("• 充分了解产品知识\n");
                tips.append("• 建立信任关系\n");
                tips.append("• 关注客户需求而非推销\n");
                tips.append("• 持续学习提升技巧\n");
        }
        
        if (productId != null && !productId.isEmpty()) {
            tips.append("\n【产品针对性建议】\n");
            tips.append("针对").append(productId).append("产品，建议重点突出：\n");
            tips.append("• 产品核心功能与客户需求的匹配点\n");
            tips.append("• 同行业成功案例\n");
            tips.append("• 产品独特卖点\n");
        }
        
        return tips.toString();
    }

    @Override
    public ToolDefinition getDefinition() {
        return definition;
    }
}
