package com.yancy.yharness.context;

import java.time.LocalDateTime;

public class TradeRecord {
    
    private String stockCode;
    private String stockName;
    private String tradeType;
    private Integer quantity;
    private Double price;
    private LocalDateTime tradeTime;
    private String reason;

    public TradeRecord() {
        this.tradeTime = LocalDateTime.now();
    }

    public TradeRecord(String stockCode, String stockName, String tradeType, Integer quantity, Double price, String reason) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = price;
        this.reason = reason;
        this.tradeTime = LocalDateTime.now();
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(LocalDateTime tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
