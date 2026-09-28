package com.gym.tracker.dto;

import com.gym.tracker.entity.AppUser;
import java.time.LocalDateTime;

/** Returned by all user-facing APIs; the password field is intentionally absent. */
public class UserResponse {

    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(AppUser user) {
        this.userId    = user.getUserId();
        this.username  = user.getUsername();
        this.fullName  = user.getFullName();
        this.role      = user.getRole().name();
        this.enabled   = user.isEnabled();
        this.createdAt = user.getCreatedAt();
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters (needed by Jackson)
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
