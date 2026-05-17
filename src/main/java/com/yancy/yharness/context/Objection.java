
package com.yancy.yharness.context;

import java.time.LocalDateTime;

public class Objection {
    
    private String content;
    private String type;
    private LocalDateTime raisedAt;
    private boolean resolved;
    private String resolution;

    public Objection() {
        this.raisedAt = LocalDateTime.now();
        this.resolved = false;
    }

    public Objection(String content, String type) {
        this.content = content;
        this.type = type;
        this.raisedAt = LocalDateTime.now();
        this.resolved = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getRaisedAt() {
        return raisedAt;
    }

    public void setRaisedAt(LocalDateTime raisedAt) {
        this.raisedAt = raisedAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
        this.resolved = true;
    }
}
