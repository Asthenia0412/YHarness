
package com.yancy.yharness.config;

import java.util.ArrayList;
import java.util.List;

public class HooksConfig {
    
    private boolean enabled = true;
    private List<String> packages = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }
}
