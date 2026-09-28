package com.gym.tracker.controller;

import com.gym.tracker.service.CheckinService;
import com.gym.tracker.service.MemberService;
import com.gym.tracker.service.MembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MemberService memberService;
    private final MembershipService membershipService;
    private final CheckinService checkinService;

    public DashboardController(MemberService memberService,
                               MembershipService membershipService,
                               CheckinService checkinService) {
        this.memberService = memberService;
        this.membershipService = membershipService;
        this.checkinService = checkinService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembers", memberService.countMembers());
        stats.put("activeMemberships", membershipService.countActiveMemberships());
        stats.put("todayCheckins", checkinService.countTodayCheckins());
        return ResponseEntity.ok(stats);
    }
}
