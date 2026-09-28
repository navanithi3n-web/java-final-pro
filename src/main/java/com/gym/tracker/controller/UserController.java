package com.gym.tracker.controller;

import com.gym.tracker.dto.UserRequest;
import com.gym.tracker.dto.UserResponse;
import com.gym.tracker.entity.AppUser;
import com.gym.tracker.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")  // All user management is ADMIN-only
public class UserController {

    private final AppUserService userService;

    public UserController(AppUserService userService) {
        this.userService = userService;
    }

    /** GET /api/users  – list all users (passwords never included). */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** GET /api/users/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(new UserResponse(userService.getUserById(id)));
    }

    /** POST /api/users – create a staff user. */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        AppUser created = userService.createUser(request);
        return new ResponseEntity<>(new UserResponse(created), HttpStatus.CREATED);
    }

    /** PUT /api/users/{id} – update name, role, enabled, or reset password. */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        AppUser updated = userService.updateUser(id, request);
        return ResponseEntity.ok(new UserResponse(updated));
    }
}
