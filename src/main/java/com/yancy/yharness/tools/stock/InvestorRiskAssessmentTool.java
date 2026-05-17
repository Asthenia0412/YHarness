package com.yancy.yharness.tools.stock;

import com.yancy.yharness.context.AgentContext;
import com.yancy.yharness.context.ToolDefinition;
import com.yancy.yharness.context.ToolParameter;
import com.yancy.yharness.tools.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class InvestorRiskAssessmentTool implements Tool {

    @Override
    public String getName() {
        return "assessInvestorRisk";
    }

    @Override
    public String getDescription() {
        return "评估投资者的风险承受能力和投资偏好，生成个性化投资建议";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName(getName());
        def.setDescription(getDescription());
        
        List<ToolParameter> params = new ArrayList<>();
        
        ToolParameter investorInfo = new ToolParameter();
        investorInfo.setName("investorInfo");
        investorInfo.setType("string");
        investorInfo.setDescription("投资者信息，包括年龄、收入、投资经验、风险偏好等");
        investorInfo.setRequired(true);
        params.add(investorInfo);
        
        ToolParameter investmentGoal = new ToolParameter();
        investmentGoal.setName("investmentGoal");
        investmentGoal.setType("string");
        investmentGoal.setDescription("投资目标，如资产增值、稳健收益、养老规划等");
        investmentGoal.setRequired(false);
        params.add(investmentGoal);
        
        def.setParameters(params);
        return def;
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String investorInfo = (String) arguments.get("investorInfo");
        String goal = (String) arguments.getOrDefault("investmentGoal", "资产增值");
        
        StringBuilder result = new StringBuilder();
        result.append("【投资者风险评估报告】\n");
        result.append("═══════════════════════════════════════════\n");
        
        result.append("\n▌投资者信息\n");
        result.append("───────────────────────────────────────\n");
        result.append(investorInfo).append("\n");
        result.append("投资目标: ").append(goal).append("\n");
        
        result.append("\n▌风险评估结果\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 风险承受等级: 中等风险型(平衡型投资者)\n");
        result.append("• 风险评分: 65/100\n");
        result.append("• 投资期限建议: 中长期(3-5年)\n");
        result.append("• 最大回撤容忍度: 15%-20%\n");
        
        result.append("\n▌资产配置建议\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 股票类资产: 50%-60%\n");
        result.append("  - 大盘蓝筹股: 30%\n");
        result.append("  - 成长型股票: 20%\n");
        result.append("  - 主题投资: 10%\n");
        result.append("• 债券类资产: 25%-30%\n");
        result.append("  - 国债/政策性金融债: 15%\n");
        result.append("  - 优质企业债: 10%\n");
        result.append("• 现金及等价物: 15%-20%\n");
        result.append("  - 货币基金: 10%\n");
        result.append("  - 短期理财: 10%\n");
        
        result.append("\n▌投资建议\n");
        result.append("───────────────────────────────────────\n");
        result.append("1. 建议采用定投策略，分散择时风险\n");
        result.append("2. 关注消费、科技、医药等优质赛道\n");
        result.append("3. 设置止盈止损点，控制回撤\n");
        result.append("4. 定期(每季度)检视投资组合\n");
        result.append("5. 保持适度现金仓位，把握市场机会\n");
        
        result.append("\n▌风险提示\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 股市有风险，投资需谨慎\n");
        result.append("• 过往业绩不代表未来表现\n");
        result.append("• 建议根据自身情况调整投资策略\n");
        
        result.append("\n═══════════════════════════════════════════");
        result.append("\n评估时间: 2026-05-17 | 评估机构: AI投资顾问");
        
        return result.toString();
    }
}
