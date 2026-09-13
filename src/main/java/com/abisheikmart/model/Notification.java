package com.abisheikmart.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String link;
    private Boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Boolean getIsRead() { return isRead != null ? isRead : false; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
