package com.example.kenny.oscarchatbot;

public class ChatMessage {
    private String content;
    private Type type;
    private String dateString;

    public ChatMessage(String content, Type type, String dateString) {
        this.content = content;
        this.type = type;
        this.dateString = dateString;
    }
    // 判斷回答人是ROBOT還是PERSON
    public enum Type
    {
        ROBOT, PERSON
    }

    public String getContent() {
        return content;
    }

    public Type getType() {
        return type;
    }

    public String getDateString() {
        return dateString;
    }
}
