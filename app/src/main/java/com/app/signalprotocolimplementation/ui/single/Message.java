package com.app.signalprotocolimplementation.ui.single;

public class Message {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    private String id;
    private String message;
    private String fromId;
    private String toId;

    public Message(String id, String message, String fromId, String toId) {
        this.id = id;
        this.message = message;
        this.fromId = fromId;
        this.toId = toId;
    }
}
