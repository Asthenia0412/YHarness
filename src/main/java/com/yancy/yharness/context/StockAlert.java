package com.yancy.yharness.context;

import java.time.LocalDateTime;

public class StockAlert {
    
    private String stockCode;
    private String alertType;
    private Double targetPrice;
    private String condition;
    private String message;
    private boolean triggered;
    private LocalDateTime createdAt;

    public StockAlert() {
        this.createdAt = LocalDateTime.now();
        this.triggered = false;
    }

    public StockAlert(String stockCode, String alertType, Double targetPrice, String condition, String message) {
        this.stockCode = stockCode;
        this.alertType = alertType;
        this.targetPrice = targetPrice;
        this.condition = condition;
        this.message = message;
        this.triggered = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
