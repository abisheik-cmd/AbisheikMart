package com.abisheikmart.dto;

public class LoginRequest {
    private String email;
    private String username;
    private String password;

    public LoginRequest() {}

    public String getEmail() { return email != null ? email : username; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

