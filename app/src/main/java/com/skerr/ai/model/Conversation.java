package com.skerr.ai.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private String id;
    private String title;
    private List<Message> messages;
    private long createdAt;
    private long updatedAt;

    public Conversation(String id, String title) {
        this.id = id;
        this.title = title;
        this.messages = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<Message> getMessages() { return messages; }
    public void addMessage(Message message) {
        messages.add(message);
        this.updatedAt = System.currentTimeMillis();
    }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
