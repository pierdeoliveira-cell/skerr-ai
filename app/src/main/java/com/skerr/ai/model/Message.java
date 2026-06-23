package com.skerr.ai.model;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    public static final int TYPE_THINKING = 2;

    private String id;
    private String content;
    private int type;
    private long timestamp;
    private boolean isThinking;
    private String thinkingContent;

    public Message(String id, String content, int type, long timestamp) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.isThinking = false;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public boolean isThinking() { return isThinking; }
    public void setThinking(boolean thinking) { isThinking = thinking; }
    public String getThinkingContent() { return thinkingContent; }
    public void setThinkingContent(String thinkingContent) { this.thinkingContent = thinkingContent; }
}
