package com.gym.tracker.service;

import com.gym.tracker.dto.CheckinRequest;
import com.gym.tracker.entity.Checkin;
import com.gym.tracker.entity.Member;
import com.gym.tracker.exception.MembershipExpiredException;
import com.gym.tracker.exception.ResourceNotFoundException;
import com.gym.tracker.repository.CheckinRepository;
import com.gym.tracker.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final MemberRepository memberRepository;
    private final MembershipService membershipService;

    public CheckinService(CheckinRepository checkinRepository,
                          MemberRepository memberRepository,
                          MembershipService membershipService) {
        this.checkinRepository = checkinRepository;
        this.memberRepository = memberRepository;
        this.membershipService = membershipService;
    }

    public Checkin processCheckin(CheckinRequest request) {
        Member member = null;

        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));
        } else if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            member = memberRepository.findByPhone(request.getPhone().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with phone: " + request.getPhone()));
        } else {
            throw new IllegalArgumentException("Either memberId or phone must be provided for check-in");
        }

        // Business Rule 3: Check-in must be REJECTED if the member has no active membership.
        // Return HTTP 400 with the message "Membership expired. Please renew."
        boolean hasActive = membershipService.hasActiveMembership(member.getMemberId());
        if (!hasActive) {
            throw new MembershipExpiredException("Membership expired. Please renew.");
        }

        // Business Rule 6: Store member and current date/time
        Checkin checkin = new Checkin(member, LocalDateTime.now());
        return checkinRepository.save(checkin);
    }

    @Transactional(readOnly = true)
    public List<Checkin> getCheckins(LocalDate date) {
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            return checkinRepository.findByCheckinTimeBetweenOrderByCheckinTimeDesc(start, end);
        }
        return checkinRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Checkin> getCheckinsByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        return checkinRepository.findByMember_MemberIdOrderByCheckinTimeDesc(memberId);
    }

    @Transactional(readOnly = true)
    public List<Checkin> getCurrentMonthCheckinsForMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentYearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        return checkinRepository.findByCheckinTimeBetweenOrderByCheckinTimeDesc(startOfMonth, endOfMonth)
                .stream()
                .filter(c -> c.getMember().getMemberId().equals(memberId))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countTodayCheckins() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return checkinRepository.countByCheckinTimeBetween(startOfDay, endOfDay);
    }
}
