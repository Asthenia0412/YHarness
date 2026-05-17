package com.yancy.yharness.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvestmentState {
    
    private InvestmentStage currentStage;
    private InvestorProfile investorProfile;
    private List<String> portfolioStocks = new ArrayList<>();
    private BigDecimal totalInvestment;
    private BigDecimal currentProfitLoss;
    private String nextAction;
    private List<TradeRecord> tradeHistory = new ArrayList<>();
    private List<StockAlert> stockAlerts = new ArrayList<>();
    private List<String> marketConcerns = new ArrayList<>();

    public InvestmentState() {
        this.currentStage = InvestmentStage.INITIAL_CONSULTATION;
        this.investorProfile = new InvestorProfile();
    }

    public InvestmentStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(InvestmentStage currentStage) {
        this.currentStage = currentStage;
    }

    public InvestorProfile getInvestorProfile() {
        return investorProfile;
    }

    public void setInvestorProfile(InvestorProfile investorProfile) {
        this.investorProfile = investorProfile;
    }

    public List<String> getPortfolioStocks() {
        return portfolioStocks;
    }

    public void setPortfolioStocks(List<String> portfolioStocks) {
        this.portfolioStocks = portfolioStocks;
    }

    public void addPortfolioStock(String stock) {
        this.portfolioStocks.add(stock);
    }

    public BigDecimal getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(BigDecimal totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public BigDecimal getCurrentProfitLoss() {
        return currentProfitLoss;
    }

    public void setCurrentProfitLoss(BigDecimal currentProfitLoss) {
        this.currentProfitLoss = currentProfitLoss;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public List<TradeRecord> getTradeHistory() {
        return tradeHistory;
    }

    public void setTradeHistory(List<TradeRecord> tradeHistory) {
        this.tradeHistory = tradeHistory;
    }

    public void addTradeRecord(TradeRecord record) {
        this.tradeHistory.add(record);
    }

    public List<StockAlert> getStockAlerts() {
        return stockAlerts;
    }

    public void setStockAlerts(List<StockAlert> stockAlerts) {
        this.stockAlerts = stockAlerts;
    }

    public void addStockAlert(StockAlert alert) {
        this.stockAlerts.add(alert);
    }

    public List<String> getMarketConcerns() {
        return marketConcerns;
    }

    public void setMarketConcerns(List<String> marketConcerns) {
        this.marketConcerns = marketConcerns;
    }

    public void addMarketConcern(String concern) {
        this.marketConcerns.add(concern);
    }

    public void advanceStage() {
        switch (currentStage) {
            case INITIAL_CONSULTATION:
                currentStage = InvestmentStage.RISK_ASSESSMENT;
                break;
            case RISK_ASSESSMENT:
                currentStage = InvestmentStage.PORTFOLIO_ANALYSIS;
                break;
            case PORTFOLIO_ANALYSIS:
                currentStage = InvestmentStage.STOCK_RESEARCH;
                break;
            case STOCK_RESEARCH:
                currentStage = InvestmentStage.INVESTMENT_DECISION;
                break;
            case INVESTMENT_DECISION:
                currentStage = InvestmentStage.POSITION_MANAGEMENT;
                break;
            default:
                break;
        }
    }
}
