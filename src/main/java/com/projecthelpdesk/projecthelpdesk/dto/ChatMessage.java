package com.projecthelpdesk.projecthelpdesk.dto;

public class ChatMessage {

    private String message;
    private String response;

    public ChatMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
