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
public class MarketNewsTool implements Tool {

    @Override
    public String getName() {
        return "getMarketNews";
    }

    @Override
    public String getDescription() {
        return "获取市场热点新闻和行业动态，帮助投资者了解市场趋势";
    }

    @Override
    public ToolDefinition getDefinition() {
        ToolDefinition def = new ToolDefinition();
        def.setName(getName());
        def.setDescription(getDescription());
        
        List<ToolParameter> params = new ArrayList<>();
        
        ToolParameter category = new ToolParameter();
        category.setName("category");
        category.setType("string");
        category.setDescription("新闻类别: market(市场), industry(行业), company(公司)");
        category.setRequired(true);
        params.add(category);
        
        ToolParameter keyword = new ToolParameter();
        keyword.setName("keyword");
        keyword.setType("string");
        keyword.setDescription("搜索关键词，可选");
        keyword.setRequired(false);
        params.add(keyword);
        
        def.setParameters(params);
        return def;
    }

    @Override
    public String execute(AgentContext context, Map<String, Object> arguments) {
        String category = (String) arguments.get("category");
        String keyword = (String) arguments.getOrDefault("keyword", "");
        
        StringBuilder result = new StringBuilder();
        result.append("【市场资讯 - ").append(getCategoryName(category)).append("】\n");
        result.append("═══════════════════════════════════════════\n");
        
        if ("market".equals(category)) {
            result.append("\n▌宏观经济\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 央行维持稳健货币政策，市场流动性充裕\n");
            result.append("• GDP增速符合预期，经济复苏态势良好\n");
            result.append("• 外资持续流入A股，北向资金净买入\n");
            
            result.append("\n▌市场动态\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 上证指数收于3150点，上涨0.85%\n");
            result.append("• 创业板指表现强劲，科技股领涨\n");
            result.append("• 两市成交额突破万亿，市场情绪回暖\n");
            
        } else if ("industry".equals(category)) {
            result.append("\n▌行业热点\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 新能源汽车销量创新高，产业链受益\n");
            result.append("• AI大模型持续突破，算力需求旺盛\n");
            result.append("• 医药创新政策利好，创新药迎来机遇\n");
            result.append("• 消费复苏明显，白酒板块表现活跃\n");
            
            result.append("\n▌板块轮动\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 强势板块: AI、新能源、医药\n");
            result.append("• 弱势板块: 地产、银行、建材\n");
            result.append("• 资金流向: 科技成长股受追捧\n");
            
        } else if ("company".equals(category)) {
            result.append("\n▌公司动态\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 贵州茅台: 提价预期升温，机构看好\n");
            result.append("• 宁德时代: 新品发布，技术领先\n");
            result.append("• 腾讯控股: 游戏业务回暖，广告增长\n");
            result.append("• 比亚迪: 销量再创新高，出海加速\n");
        }
        
        if (!keyword.isEmpty()) {
            result.append("\n▌关键词相关: ").append(keyword).append("\n");
            result.append("───────────────────────────────────────\n");
            result.append("• 找到 ").append(keyword).append(" 相关资讯 5 条\n");
            result.append("• 建议关注相关产业链投资机会\n");
        }
        
        result.append("\n═══════════════════════════════════════════");
        result.append("\n资讯更新时间: 2026-05-17 15:00:00");
        
        return result.toString();
    }
    
    private String getCategoryName(String category) {
        switch (category) {
            case "market": return "市场要闻";
            case "industry": return "行业动态";
            case "company": return "公司资讯";
            default: return category;
        }
    }
}
