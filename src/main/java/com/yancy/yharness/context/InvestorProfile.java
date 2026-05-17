package com.yancy.yharness.context;

import java.util.ArrayList;
import java.util.List;

public class InvestorProfile {
    
    private String name;
    private String riskTolerance;
    private String investmentExperience;
    private String investmentGoal;
    private String investmentHorizon;
    private String availableFunds;
    private List<String> preferredSectors = new ArrayList<>();
    private List<String> watchlist = new ArrayList<>();
    private List<String> investmentConcerns = new ArrayList<>();

    public InvestorProfile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(String riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public String getInvestmentExperience() {
        return investmentExperience;
    }

    public void setInvestmentExperience(String investmentExperience) {
        this.investmentExperience = investmentExperience;
    }

    public String getInvestmentGoal() {
        return investmentGoal;
    }

    public void setInvestmentGoal(String investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public String getInvestmentHorizon() {
        return investmentHorizon;
    }

    public void setInvestmentHorizon(String investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }

    public String getAvailableFunds() {
        return availableFunds;
    }

    public void setAvailableFunds(String availableFunds) {
        this.availableFunds = availableFunds;
    }

    public List<String> getPreferredSectors() {
        return preferredSectors;
    }

    public void setPreferredSectors(List<String> preferredSectors) {
        this.preferredSectors = preferredSectors;
    }

    public void addPreferredSector(String sector) {
        this.preferredSectors.add(sector);
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<String> watchlist) {
        this.watchlist = watchlist;
    }

    public void addToWatchlist(String stock) {
        this.watchlist.add(stock);
    }

    public List<String> getInvestmentConcerns() {
        return investmentConcerns;
    }

    public void setInvestmentConcerns(List<String> investmentConcerns) {
        this.investmentConcerns = investmentConcerns;
    }

    public void addInvestmentConcern(String concern) {
        this.investmentConcerns.add(concern);
    }
}
