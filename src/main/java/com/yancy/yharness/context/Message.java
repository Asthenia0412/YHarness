
package com.yancy.yharness.context;

import java.time.LocalDateTime;

public class Message {
    
    private MessageRole role;
    private String content;
    private LocalDateTime timestamp;
    private String name;

    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(MessageRole role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public Message(MessageRole role, String content, String name) {
        this.role = role;
        this.content = content;
        this.name = name;
        this.timestamp = LocalDateTime.now();
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
