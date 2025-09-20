package com.loramaster.core;

public class Session {
    private String sessionId;
    private String sessionName;
    private String startTimestamp;
    private String lastTimestamp;
    private int dotsCount;

    
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getSessionName() {
        return sessionName;
    }
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    public String getStartTimestamp() {
        return startTimestamp;
    }
    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
    public String getLastTimestamp() {
        return lastTimestamp;
    }
    public void setLastTimestamp(String lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
    public int getDotsCount() {
        return dotsCount;
    }
    public void setDotsCount(int dotsCount) {
        this.dotsCount = dotsCount;
    }
}
