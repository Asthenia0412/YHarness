
package com.yancy.yharness.context;

import java.time.LocalDateTime;

public class ContactRecord {
    
    private LocalDateTime contactTime;
    private String contactMethod;
    private String content;
    private String outcome;

    public ContactRecord() {
        this.contactTime = LocalDateTime.now();
    }

    public ContactRecord(String contactMethod, String content, String outcome) {
        this.contactTime = LocalDateTime.now();
        this.contactMethod = contactMethod;
        this.content = content;
        this.outcome = outcome;
    }

    public LocalDateTime getContactTime() {
        return contactTime;
    }

    public void setContactTime(LocalDateTime contactTime) {
        this.contactTime = contactTime;
    }

    public String getContactMethod() {
        return contactMethod;
    }

    public void setContactMethod(String contactMethod) {
        this.contactMethod = contactMethod;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
