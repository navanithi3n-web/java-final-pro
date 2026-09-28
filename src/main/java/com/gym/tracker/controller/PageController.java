package com.gym.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * PageController forwards clean browser URLs to static HTML files.
 * It is a plain @Controller (not @RestController) so that Spring MVC serves
 * the HTML files directly from src/main/resources/static.
 * <p>
 * Security note: every mapping here (except /login) requires authentication –
 * that is enforced by SecurityConfig, not here.
 */
@Controller
public class PageController {

    // ── Auth ─────────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login() { return "forward:/login.html"; }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() { return "forward:/dashboard.html"; }

    // ── Members ───────────────────────────────────────────────────────────────
    @GetMapping("/members")
    public String members() { return "forward:/members.html"; }

    @GetMapping("/members/new")
    public String memberNew() { return "forward:/member-form.html"; }

    @GetMapping("/members/{id}")
    public String memberDetail(@PathVariable String id) { return "forward:/member-detail.html"; }

    @GetMapping("/members/{id}/edit")
    public String memberEdit(@PathVariable String id) { return "forward:/member-form.html"; }

    // ── Plans ─────────────────────────────────────────────────────────────────
    @GetMapping("/plans")
    public String plans() { return "forward:/plans.html"; }

    @GetMapping("/plans/{id}")
    public String planDetail(@PathVariable String id) { return "forward:/plan-detail.html"; }

    // ── Memberships ───────────────────────────────────────────────────────────
    @GetMapping("/memberships")
    public String memberships() { return "forward:/memberships.html"; }

    @GetMapping("/memberships/new")
    public String membershipNew() { return "forward:/membership-form.html"; }

    @GetMapping("/memberships/{id}")
    public String membershipDetail(@PathVariable String id) { return "forward:/membership-detail.html"; }

    // ── Check-In ──────────────────────────────────────────────────────────────
    @GetMapping("/checkin")
    public String checkin() { return "forward:/checkin.html"; }

    // ── Attendance ────────────────────────────────────────────────────────────
    @GetMapping("/attendance")
    public String attendance() { return "forward:/attendance.html"; }

    @GetMapping("/attendance/member/{id}")
    public String memberAttendance(@PathVariable String id) { return "forward:/member-attendance.html"; }

    // ── Users (ADMIN only, enforced by SecurityConfig) ────────────────────────
    @GetMapping("/users")
    public String users() { return "forward:/users.html"; }
}
