
package com.example.yharness.context;

import java.util.ArrayList;
import java.util.List;

public class CustomerProfile {
    
    private String name;
    private String company;
    private String title;
    private String email;
    private String phone;
    private String industry;
    private String budgetRange;
    private boolean decisionMaker;
    private List<String> painPoints = new ArrayList<>();

    public CustomerProfile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(String budgetRange) {
        this.budgetRange = budgetRange;
    }

    public boolean isDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(boolean decisionMaker) {
        this.decisionMaker = decisionMaker;
    }

    public List<String> getPainPoints() {
        return painPoints;
    }

    public void setPainPoints(List<String> painPoints) {
        this.painPoints = painPoints;
    }

    public void addPainPoint(String painPoint) {
        this.painPoints.add(painPoint);
    }
}
