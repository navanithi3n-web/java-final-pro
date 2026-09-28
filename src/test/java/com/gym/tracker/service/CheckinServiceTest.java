package com.gym.tracker.service;

import com.gym.tracker.dto.CheckinRequest;
import com.gym.tracker.entity.Checkin;
import com.gym.tracker.entity.Member;
import com.gym.tracker.entity.Membership;
import com.gym.tracker.entity.Plan;
import com.gym.tracker.exception.MembershipExpiredException;
import com.gym.tracker.exception.ResourceNotFoundException;
import com.gym.tracker.repository.CheckinRepository;
import com.gym.tracker.repository.MemberRepository;
import com.gym.tracker.repository.MembershipRepository;
import com.gym.tracker.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CheckinServiceTest {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    private Member activeMember;
    private Member expiredMember;
    private Member noPlanMember;
    private Plan monthlyPlan;

    @BeforeEach
    public void setup() {
        checkinRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();

        monthlyPlan = planRepository.findByPlanName("Monthly")
                .orElseGet(() -> planRepository.save(new Plan("Monthly", 30, 1000.0)));

        // 1. Member with active membership
        activeMember = memberRepository.save(new Member("Alice Smith", "9876543211", "alice@example.com"));
        Membership activeMembership = new Membership(
                activeMember,
                monthlyPlan,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(25)
        );
        membershipRepository.save(activeMembership);

        // 2. Member with expired membership
        expiredMember = memberRepository.save(new Member("Bob Jones", "9876543212", "bob@example.com"));
        Membership expiredMembership = new Membership(
                expiredMember,
                monthlyPlan,
                LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(10)
        );
        membershipRepository.save(expiredMembership);

        // 3. Member with no membership
        noPlanMember = memberRepository.save(new Member("Charlie Brown", "9876543213", "charlie@example.com"));
    }

    @Test
    @DisplayName("Check-in with an active membership -> success and the record is saved")
    public void testCheckinActiveMemberSuccess() {
        CheckinRequest request = new CheckinRequest(activeMember.getMemberId(), null);
        Checkin checkin = checkinService.processCheckin(request);

        assertNotNull(checkin.getCheckinId());
        assertEquals(activeMember.getMemberId(), checkin.getMember().getMemberId());
        assertNotNull(checkin.getCheckinTime());

        List<Checkin> memberCheckins = checkinRepository.findByMember_MemberIdOrderByCheckinTimeDesc(activeMember.getMemberId());
        assertEquals(1, memberCheckins.size());
    }

    @Test
    @DisplayName("Check-in with phone number for active member -> success")
    public void testCheckinByPhoneSuccess() {
        CheckinRequest request = new CheckinRequest(null, activeMember.getPhone());
        Checkin checkin = checkinService.processCheckin(request);

        assertNotNull(checkin.getCheckinId());
        assertEquals(activeMember.getMemberId(), checkin.getMember().getMemberId());
    }

    @Test
    @DisplayName("Check-in with an expired membership -> rejected with 400 (MembershipExpiredException)")
    public void testCheckinExpiredMemberRejected() {
        CheckinRequest request = new CheckinRequest(expiredMember.getMemberId(), null);

        MembershipExpiredException exception = assertThrows(
                MembershipExpiredException.class,
                () -> checkinService.processCheckin(request)
        );

        assertEquals("Membership expired. Please renew.", exception.getMessage());
    }

    @Test
    @DisplayName("Check-in with member with no membership -> rejected with 400")
    public void testCheckinNoMembershipRejected() {
        CheckinRequest request = new CheckinRequest(noPlanMember.getMemberId(), null);

        MembershipExpiredException exception = assertThrows(
                MembershipExpiredException.class,
                () -> checkinService.processCheckin(request)
        );

        assertEquals("Membership expired. Please renew.", exception.getMessage());
    }

    @Test
    @DisplayName("Check-in with an invalid member ID -> 404 (ResourceNotFoundException)")
    public void testCheckinInvalidMemberId() {
        CheckinRequest request = new CheckinRequest(999999L, null);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> checkinService.processCheckin(request)
        );

        assertTrue(exception.getMessage().contains("Member not found with id: 999999"));
    }
}
