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
public class StockQuoteTool implements Tool {

    @Override
    public String getName() {
        return "getStockQuote";
    }

    @Override
    public String getDescription() {
        return "获取股票实时行情信息，包括当前价格、涨跌幅、成交量等";
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
        stockCode.setDescription("股票代码，如 600519、000001、AAPL 等");
        stockCode.setRequired(true);
        params.add(stockCode);
        
        def.setParameters(params);
        return def;
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String stockCode = (String) arguments.get("stockCode");
        
        StringBuilder result = new StringBuilder();
        result.append("【股票行情 - ").append(stockCode).append("】\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        if ("600519".equals(stockCode)) {
            result.append("股票名称: 贵州茅台\n");
            result.append("当前价格: ¥1856.00\n");
            result.append("涨跌幅: +2.35%\n");
            result.append("涨跌额: +42.50\n");
            result.append("今开: ¥1820.00\n");
            result.append("昨收: ¥1813.50\n");
            result.append("最高: ¥1865.00\n");
            result.append("最低: ¥1815.00\n");
            result.append("成交量: 125.6万手\n");
            result.append("成交额: 23.2亿\n");
            result.append("市盈率(PE): 32.5\n");
            result.append("市净率(PB): 9.8\n");
            result.append("总市值: 2.33万亿\n");
        } else if ("000001".equals(stockCode)) {
            result.append("股票名称: 平安银行\n");
            result.append("当前价格: ¥11.25\n");
            result.append("涨跌幅: -0.88%\n");
            result.append("涨跌额: -0.10\n");
            result.append("今开: ¥11.35\n");
            result.append("昨收: ¥11.35\n");
            result.append("最高: ¥11.42\n");
            result.append("最低: ¥11.18\n");
            result.append("成交量: 856.2万手\n");
            result.append("成交额: 9.6亿\n");
            result.append("市盈率(PE): 5.2\n");
            result.append("市净率(PB): 0.6\n");
            result.append("总市值: 2185亿\n");
        } else if ("AAPL".equalsIgnoreCase(stockCode)) {
            result.append("股票名称: Apple Inc.\n");
            result.append("当前价格: $178.52\n");
            result.append("涨跌幅: +1.25%\n");
            result.append("涨跌额: +$2.21\n");
            result.append("今开: $176.80\n");
            result.append("昨收: $176.31\n");
            result.append("最高: $179.35\n");
            result.append("最低: $176.15\n");
            result.append("成交量: 52.3M\n");
            result.append("市盈率(PE): 28.6\n");
            result.append("市净率(PB): 45.2\n");
            result.append("总市值: 2.78万亿\n");
        } else {
            result.append("股票名称: ").append(stockCode).append("\n");
            result.append("当前价格: ¥XX.XX\n");
            result.append("涨跌幅: X.XX%\n");
            result.append("成交量: XXX万手\n");
            result.append("提示: 该股票数据为模拟数据，实际使用请接入真实行情API\n");
        }
        
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("数据更新时间: 2026-05-17 15:00:00");
        
        return result.toString();
    }
}
