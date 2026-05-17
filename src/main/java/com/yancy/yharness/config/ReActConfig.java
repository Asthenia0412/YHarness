
package com.yancy.yharness.config;

public class ReActConfig {
    
    private int maxIterations = 10;
    private boolean enableThinking = true;

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public boolean isEnableThinking() {
        return enableThinking;
    }

    public void setEnableThinking(boolean enableThinking) {
        this.enableThinking = enableThinking;
    }
}
