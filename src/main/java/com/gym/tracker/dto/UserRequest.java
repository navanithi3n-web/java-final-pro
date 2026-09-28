package com.gym.tracker.dto;

import com.gym.tracker.entity.AppUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    // Password may be absent on updates (null = keep existing)
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "ADMIN|STAFF", message = "Role must be ADMIN or STAFF")
    private String role;

    private Boolean enabled;

    public UserRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public AppUser.Role resolvedRole() {
        return role == null ? AppUser.Role.STAFF : AppUser.Role.valueOf(role.toUpperCase());
    }
}
