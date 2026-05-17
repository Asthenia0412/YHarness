package com.yancy.yharness.context;

public enum InvestmentStage {
    INITIAL_CONSULTATION("初步咨询"),
    RISK_ASSESSMENT("风险评估"),
    PORTFOLIO_ANALYSIS("持仓分析"),
    STOCK_RESEARCH("股票研究"),
    INVESTMENT_DECISION("投资决策"),
    POSITION_MANAGEMENT("仓位管理");

    private final String description;

    InvestmentStage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
