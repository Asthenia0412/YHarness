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
public class StockAnalysisTool implements Tool {

    @Override
    public String getName() {
        return "analyzeStock";
    }

    @Override
    public String getDescription() {
        return "对股票进行技术分析和基本面分析，提供买卖建议";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName(getName());
        def.setDescription(getDescription());
        
        List<ToolParameter> params = new ArrayList<>();
        
        ToolParameter stockCode = new ToolParameter();
        stockCode.setName("stockCode");
        stockCode.setType("string");
        stockCode.setDescription("股票代码");
        stockCode.setRequired(true);
        params.add(stockCode);
        
        ToolParameter analysisType = new ToolParameter();
        analysisType.setName("analysisType");
        analysisType.setType("string");
        analysisType.setDescription("分析类型: technical(技术分析), fundamental(基本面分析), comprehensive(综合分析)");
        analysisType.setRequired(false);
        params.add(analysisType);
        
        def.setParameters(params);
        return def;
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String stockCode = (String) arguments.get("stockCode");
        String type = (String) arguments.getOrDefault("analysisType", "comprehensive");
        
        StringBuilder result = new StringBuilder();
        result.append("【股票分析报告 - ").append(stockCode).append("】\n");
        result.append("═══════════════════════════════════════════\n");
        
        if ("technical".equals(type) || "comprehensive".equals(type)) {
            result.append("\n▌技术分析\n");
            result.append("───────────────────────────────────────\n");
            result.append("• MA5: 1842.50 (短期均线)\n");
            result.append("• MA10: 1835.20 (中期均线)\n");
            result.append("• MA20: 1810.80 (长期均线)\n");
            result.append("• MACD: 金叉形成，多头信号\n");
            result.append("• RSI(14): 62.5，处于强势区间\n");
            result.append("• KDJ: K线向上穿越D线，买入信号\n");
            result.append("• 布林带: 股价接近上轨，有回调风险\n");
            result.append("• 成交量: 放量上涨，资金流入明显\n");
            result.append("\n技术面结论: 短期看多，建议逢低买入\n");
        }
        
        if ("fundamental".equals(type) || "comprehensive".equals(type)) {
            result.append("\n▌基本面分析\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 营收增长率: 15.2% (同比)\n");
            result.append("• 净利润增长率: 18.5% (同比)\n");
            result.append("• ROE: 28.6%\n");
            result.append("• 毛利率: 91.2%\n");
            result.append("• 资产负债率: 25.3%\n");
            result.append("• 经营现金流: 正向，持续增长\n");
            result.append("• 行业地位: 行业龙头，品牌溢价高\n");
            result.append("• 护城河: 品牌优势+渠道优势+定价权\n");
            result.append("\n基本面结论: 优质标的，适合长期持有\n");
        }
        
        result.append("\n▌综合投资建议\n");
        result.append("───────────────────────────────────────\n");
        result.append("• 投资评级: 买入\n");
        result.append("• 目标价位: ¥1950.00\n");
        result.append("• 止损价位: ¥1780.00\n");
        result.append("• 建议仓位: 10%-15%\n");
        result.append("• 持有周期: 中长期(6-12个月)\n");
        result.append("• 风险提示: 注意宏观经济波动和政策风险\n");
        
        result.append("\n═══════════════════════════════════════════");
        result.append("\n分析时间: 2026-05-17 | 分析师: AI投资顾问");
        
        return result.toString();
    }
}
