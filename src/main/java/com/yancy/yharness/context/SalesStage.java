
package com.yancy.yharness.context;

public enum SalesStage {
    INITIAL_CONTACT("初步接触"),
    NEEDS_ANALYSIS("需求分析"),
    SOLUTION_DEMO("方案演示"),
    COMMERCIAL_NEGOTIATION("商务谈判"),
    CLOSING("成交"),
    FOLLOW_UP("跟进");

    private final String description;

    SalesStage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
