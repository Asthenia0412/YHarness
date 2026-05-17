
package com.example.yharness.core;

public class Thought {
    
    private String content;
    private Action action;
    private boolean isFinal;

    public Thought() {
    }

    public Thought(String content, Action action) {
        this.content = content;
        this.action = action;
        this.isFinal = action != null && action.isFinish();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
        this.isFinal = action != null && action.isFinish();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
}
