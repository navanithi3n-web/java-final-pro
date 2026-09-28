package com.gym.tracker.controller;

import com.gym.tracker.dto.CheckinRequest;
import com.gym.tracker.dto.CheckinResponse;
import com.gym.tracker.entity.Checkin;
import com.gym.tracker.service.CheckinService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkins")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    public ResponseEntity<CheckinResponse> processCheckin(@RequestBody CheckinRequest request) {
        Checkin checkin = checkinService.processCheckin(request);
        return new ResponseEntity<>(new CheckinResponse(checkin), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CheckinResponse>> getCheckins(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CheckinResponse> list = checkinService.getCheckins(date).stream()
                .map(CheckinResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CheckinResponse>> getCheckinsByMemberId(@PathVariable Long memberId) {
        List<CheckinResponse> list = checkinService.getCheckinsByMemberId(memberId).stream()
                .map(CheckinResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/member/{memberId}/month")
    public ResponseEntity<List<CheckinResponse>> getCurrentMonthCheckinsForMember(@PathVariable Long memberId) {
        List<CheckinResponse> list = checkinService.getCurrentMonthCheckinsForMember(memberId).stream()
                .map(CheckinResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/today-count")
    public ResponseEntity<Map<String, Long>> getTodayCheckinCount() {
        long count = checkinService.countTodayCheckins();
        return ResponseEntity.ok(Map.of("todayCheckins", count));
    }
}
