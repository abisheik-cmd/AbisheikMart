package com.abisheikmart.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class AdminUserSummary implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private Timestamp createdAt;

    public AdminUserSummary(Long id, String name, String email, String role, boolean active, Timestamp createdAt) {
        this.id = id; this.name = name; this.email = email; this.role = role; this.active = active; this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public Timestamp getCreatedAt() { return createdAt; }
}
