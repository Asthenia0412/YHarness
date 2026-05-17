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
public class InvestmentAdviceTool implements Tool {

    @Override
    public String getName() {
        return "generateInvestmentAdvice";
    }

    @Override
    public String getDescription() {
        return "根据投资者情况和市场状况，生成个性化的投资建议和策略";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName(getName());
        def.setDescription(getDescription());
        
        List<ToolParameter> params = new ArrayList<>();
        
        ToolParameter scenario = new ToolParameter();
        scenario.setName("scenario");
        scenario.setType("string");
        scenario.setDescription("投资场景: 新手入门、资产配置、调仓换股、止损止盈");
        scenario.setRequired(true);
        params.add(scenario);
        
        ToolParameter investorType = new ToolParameter();
        investorType.setName("investorType");
        investorType.setType("string");
        investorType.setDescription("投资者类型: 保守型、稳健型、激进型");
        investorType.setRequired(true);
        params.add(investorType);
        
        ToolParameter currentHoldings = new ToolParameter();
        currentHoldings.setName("currentHoldings");
        currentHoldings.setType("string");
        currentHoldings.setDescription("当前持仓情况，可选");
        currentHoldings.setRequired(false);
        params.add(currentHoldings);
        
        def.setParameters(params);
        return def;
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String scenario = (String) arguments.get("scenario");
        String investorType = (String) arguments.get("investorType");
        String holdings = (String) arguments.getOrDefault("currentHoldings", "未提供");
        
        StringBuilder result = new StringBuilder();
        result.append("【投资建议报告】\n");
        result.append("═══════════════════════════════════════════\n");
        
        result.append("\n▌场景分析\n");
        result.append("───────────────────────────────────────\n");
        result.append("投资场景: ").append(scenario).append("\n");
        result.append("投资者类型: ").append(investorType).append("\n");
        result.append("当前持仓: ").append(holdings).append("\n");
        
        result.append("\n▌投资策略建议\n");
        result.append("───────────────────────────────────────\n");
        
        if ("新手入门".equals(scenario)) {
            result.append("1. 从指数基金开始，分散风险\n");
            result.append("2. 采用定投策略，平摊成本\n");
            result.append("3. 学习基础知识，逐步深入\n");
            result.append("4. 控制仓位，不要满仓操作\n");
            result.append("5. 设置止损点，保护本金\n");
        } else if ("资产配置".equals(scenario)) {
            result.append("1. 根据风险偏好确定股债比例\n");
            result.append("2. 股票部分分散到多个行业\n");
            result.append("3. 保留10-20%现金应对机会\n");
            result.append("4. 定期再平衡，保持配置比例\n");
            result.append("5. 关注宏观经济，适时调整\n");
        } else if ("调仓换股".equals(scenario)) {
            result.append("1. 分析当前持仓，识别弱势股\n");
            result.append("2. 研究目标股票，确认投资逻辑\n");
            result.append("3. 分批调仓，降低冲击成本\n");
            result.append("4. 注意交易成本和税费影响\n");
            result.append("5. 记录调仓原因，便于复盘\n");
        } else if ("止损止盈".equals(scenario)) {
            result.append("1. 设定明确的止损位(如-10%)\n");
            result.append("2. 设定合理的止盈位(如+30%)\n");
            result.append("3. 移动止损保护利润\n");
            result.append("4. 分批止盈，锁定收益\n");
            result.append("5. 执行纪律，避免情绪干扰\n");
        }
        
        result.append("\n▌风险控制要点\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 单只股票仓位不超过总资产的15%\n");
        result.append("• 单一行业仓位不超过总资产的30%\n");
        result.append("• 设置最大回撤警戒线\n");
        result.append("• 保持充足的现金储备\n");
        result.append("• 定期检视投资组合\n");
        
        result.append("\n▌操作提醒\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 投资有风险，决策需谨慎\n");
        result.append("• 建议结合自身情况调整策略\n");
        result.append("• 如有疑问可进一步咨询\n");
        
        result.append("\n═══════════════════════════════════════════");
        result.append("\n生成时间: 2026-05-17 | AI投资顾问");
        
        return result.toString();
    }
}
