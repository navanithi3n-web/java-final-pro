package com.gym.tracker.controller;

import com.gym.tracker.dto.LoginRequest;
import com.gym.tracker.dto.UserResponse;
import com.gym.tracker.entity.AppUser;
import com.gym.tracker.service.AppUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserService userService;

    public AuthController(AuthenticationManager authenticationManager, AppUserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    /** POST /api/auth/login  – returns 200 + user info on success, 401 on wrong credentials. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Persist the SecurityContext in the HTTP session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            AppUser user = userService.getUserByUsername(request.getUsername());
            return ResponseEntity.ok(new UserResponse(user));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "error", "Unauthorized", "message", "Invalid username or password"));
        }
    }

    /** POST /api/auth/logout – invalidates the session and returns 200. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /** GET /api/auth/me – returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "error", "Unauthorized", "message", "Not logged in"));
        }
        AppUser user = userService.getUserByUsername(auth.getName());
        return ResponseEntity.ok(new UserResponse(user));
    }
}
