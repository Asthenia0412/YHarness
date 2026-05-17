
package com.yancy.yharness.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BusinessState {
    
    private SalesStage currentStage;
    private CustomerProfile customerProfile;
    private List<String> productInterest = new ArrayList<>();
    private BigDecimal dealValue;
    private String nextAction;
    private List<ContactRecord> contactHistory = new ArrayList<>();
    private List<Objection> objections = new ArrayList<>();
    private List<String> competitors = new ArrayList<>();

    public BusinessState() {
        this.currentStage = SalesStage.INITIAL_CONTACT;
        this.customerProfile = new CustomerProfile();
    }

    public SalesStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(SalesStage currentStage) {
        this.currentStage = currentStage;
    }

    public CustomerProfile getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }

    public List<String> getProductInterest() {
        return productInterest;
    }

    public void setProductInterest(List<String> productInterest) {
        this.productInterest = productInterest;
    }

    public void addProductInterest(String product) {
        this.productInterest.add(product);
    }

    public BigDecimal getDealValue() {
        return dealValue;
    }

    public void setDealValue(BigDecimal dealValue) {
        this.dealValue = dealValue;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public List<ContactRecord> getContactHistory() {
        return contactHistory;
    }

    public void setContactHistory(List<ContactRecord> contactHistory) {
        this.contactHistory = contactHistory;
    }

    public void addContactRecord(ContactRecord record) {
        this.contactHistory.add(record);
    }

    public List<Objection> getObjections() {
        return objections;
    }

    public void setObjections(List<Objection> objections) {
        this.objections = objections;
    }

    public void addObjection(Objection objection) {
        this.objections.add(objection);
    }

    public List<String> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<String> competitors) {
        this.competitors = competitors;
    }

    public void addCompetitor(String competitor) {
        this.competitors.add(competitor);
    }

    public void advanceStage() {
        switch (currentStage) {
            case INITIAL_CONTACT:
                currentStage = SalesStage.NEEDS_ANALYSIS;
                break;
            case NEEDS_ANALYSIS:
                currentStage = SalesStage.SOLUTION_DEMO;
                break;
            case SOLUTION_DEMO:
                currentStage = SalesStage.COMMERCIAL_NEGOTIATION;
                break;
            case COMMERCIAL_NEGOTIATION:
                currentStage = SalesStage.CLOSING;
                break;
            case CLOSING:
                currentStage = SalesStage.FOLLOW_UP;
                break;
            default:
                break;
        }
    }
}
