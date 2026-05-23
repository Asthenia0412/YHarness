package com.yancy.yharness.provider;

import com.yancy.yharness.model.TokenUsage;

import java.util.ArrayList;
import java.util.List;

public class MockProvider implements ModelProvider {

    private static int callCounter = 0;

    @Override
    public ChatResponse chat(ChatRequest request) {
        callCounter++;
        ChatResponse response = new ChatResponse();
        response.setContent(generateMockResponse(request));
        response.setTokenUsage(new TokenUsage(100 + callCounter * 10, 50 + callCounter * 5));

        boolean hasToolCalls = request.getMessages() != null
                && request.getMessages().stream()
                .noneMatch(m -> "tool".equals(m.getRole()));

        if (hasToolCalls && request.getTools() != null && !request.getTools().isEmpty()) {
            ToolCall toolCall = new ToolCall();
            toolCall.setId("mock_call_" + callCounter);
            toolCall.setName(request.getTools().get(0).getName());
            toolCall.setArguments("{\"query\":\"mock_query\"}");
            response.setToolCalls(java.util.Collections.singletonList(toolCall));
            response.setFinal(false);
        } else {
            response.setFinal(true);
            response.setFinishReason("stop");
        }

        return response;
    }

    @Override
    public String getName() {
        return "MockProvider";
    }

    @Override
    public boolean supports(String providerType) {
        return "MOCK".equalsIgnoreCase(providerType);
    }

    private String generateMockResponse(ChatRequest request) {
        String userMsg = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            List<ChatMessage> msgs = request.getMessages();
            userMsg = msgs.get(msgs.size() - 1).getContent();
        }

        if (userMsg == null) userMsg = "";

        if (userMsg.contains("stock") || userMsg.contains("股价") || userMsg.contains("行情")) {
            return "Based on the stock data I've retrieved, here's the latest market information.\n\nThe stock is currently trading at a stable range with moderate volume. Technical indicators suggest a neutral outlook in the short term.";
        } else if (userMsg.contains("order") || userMsg.contains("订单") || userMsg.contains("广告")) {
            return "I've checked the advertising campaign performance. Here's a summary:\n- Impressions: 45,230\n- Clicks: 1,234\n- CTR: 2.73%\n- Spend: $1,250.00\n\nThe campaign is performing within expected parameters.";
        } else if (userMsg.contains("customer") || userMsg.contains("客户") || userMsg.contains("CRM")) {
            return "Here's the customer profile information:\n- Industry: Retail\n- Account tier: Premium\n- Tenure: 24 months\n- Last contact: 3 days ago\n- Engagement score: 85/100";
        } else {
            return "I understand your inquiry. Let me help you with that. Based on the available information, I recommend proceeding with the standard approach for this scenario. Would you like more specific details on any particular aspect?";
        }
    }
}