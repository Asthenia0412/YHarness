package com.yancy.yharness.preparer;

import com.yancy.yharness.model.AgentRequest;
import com.yancy.yharness.model.TaskType;
import com.yancy.yharness.scheduler.ClientTaskJob;
import org.springframework.stereotype.Component;

@Component
public class RequestPreparer {

    public AgentRequest prepare(ClientTaskJob job) {
        AgentRequest request = new AgentRequest();
        request.setUserId(job.getUserId());
        request.setChannelAccountId(job.getChannelAccountId());
        request.setAccountId(job.getAccountId());
        request.setConversationId(job.getUserId() + "_" + job.getChannelAccountId());

        if ("INBOUND_MESSAGE".equals(job.getTaskName())) {
            request.setTaskType(TaskType.INBOUND);
            request.setUserMessage("User sent an inbound message");
        } else if ("OUTREACH_REPORT_PREP".equals(job.getTaskName()) || "OUTREACH".equals(job.getTaskName())) {
            request.setTaskType(TaskType.OUTREACH);
            request.setUserMessage("");
        } else {
            request.setTaskType(TaskType.TASK_TRACING);
            request.setUserMessage("");
        }

        request.setLanguageCode("en");
        request.setChannelId("whatsapp");
        request.setTimezone("Asia/Bangkok");

        if (job.getStrategySummary() != null) {
            request.setMetadata(new java.util.HashMap<>());
            request.getMetadata().put("strategy", job.getStrategySummary());
        }

        return request;
    }

    public AgentRequest prepareFromMessage(String userId, String userMessage, String conversationId) {
        AgentRequest request = new AgentRequest();
        request.setUserId(userId);
        request.setUserMessage(userMessage);
        request.setConversationId(conversationId);
        request.setTaskType(TaskType.INBOUND);
        request.setLanguageCode("en");
        request.setChannelId("whatsapp");
        request.setChannelAccountId("default");
        request.setTimezone("Asia/Bangkok");
        return request;
    }
}