package com.gym.tracker.controller;

import com.gym.tracker.dto.MembershipRequest;
import com.gym.tracker.dto.MembershipResponse;
import com.gym.tracker.dto.RenewMembershipRequest;
import com.gym.tracker.entity.Membership;
import com.gym.tracker.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> createMembership(@Valid @RequestBody MembershipRequest request) {
        Membership created = membershipService.createMembership(request);
        return new ResponseEntity<>(new MembershipResponse(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getAllMemberships() {
        List<MembershipResponse> list = membershipService.getAllMemberships().stream()
                .map(MembershipResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getMembershipById(@PathVariable Long id) {
        Membership membership = membershipService.getMembershipById(id);
        return ResponseEntity.ok(new MembershipResponse(membership));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<MembershipResponse> renewMembership(
            @PathVariable Long id,
            @RequestBody(required = false) RenewMembershipRequest request) {
        Membership renewed = membershipService.renewMembership(id, request);
        return ResponseEntity.ok(new MembershipResponse(renewed));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MembershipResponse>> getMembershipsByMemberId(@PathVariable Long memberId) {
        List<MembershipResponse> list = membershipService.getMembershipsByMemberId(memberId).stream()
                .map(MembershipResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
