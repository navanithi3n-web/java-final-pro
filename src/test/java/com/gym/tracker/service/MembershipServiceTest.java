package com.gym.tracker.service;

import com.gym.tracker.dto.MembershipRequest;
import com.gym.tracker.dto.RenewMembershipRequest;
import com.gym.tracker.entity.Member;
import com.gym.tracker.entity.Membership;
import com.gym.tracker.entity.Plan;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MembershipServiceTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private Member savedMember;
    private Plan monthlyPlan;
    private Plan quarterlyPlan;

    @BeforeEach
    public void setup() {
        membershipRepository.deleteAll();
        memberRepository.deleteAll();

        savedMember = memberRepository.save(new Member("John Doe", "9876543210", "john@example.com"));

        monthlyPlan = planRepository.findByPlanName("Monthly")
                .orElseGet(() -> planRepository.save(new Plan("Monthly", 30, 1000.0)));

        quarterlyPlan = planRepository.findByPlanName("Quarterly")
                .orElseGet(() -> planRepository.save(new Plan("Quarterly", 90, 2700.0)));
    }

    @Test
    @DisplayName("Create a membership -> expiryDate is correct")
    public void testCreateMembershipExpiryCorrect() {
        LocalDate startDate = LocalDate.now();
        MembershipRequest request = new MembershipRequest(savedMember.getMemberId(), monthlyPlan.getPlanId(), startDate);

        Membership created = membershipService.createMembership(request);

        assertNotNull(created.getMembershipId());
        assertEquals(startDate, created.getStartDate());
        assertEquals(startDate.plusDays(30), created.getExpiryDate());
        assertEquals("ACTIVE", created.getStatus());
        assertTrue(created.isActive());
    }

    @Test
    @DisplayName("Renew an active membership -> expiry extended from the old expiryDate")
    public void testRenewActiveMembership() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate oldExpiry = startDate.plusDays(30); // 20 days remaining

        Membership membership = new Membership(savedMember, monthlyPlan, startDate, oldExpiry);
        membership = membershipRepository.save(membership);

        RenewMembershipRequest renewRequest = new RenewMembershipRequest(monthlyPlan.getPlanId());
        Membership renewed = membershipService.renewMembership(membership.getMembershipId(), renewRequest);

        LocalDate expectedExpiry = oldExpiry.plusDays(30);
        assertEquals(expectedExpiry, renewed.getExpiryDate());
        assertEquals("ACTIVE", renewed.getStatus());
    }

    @Test
    @DisplayName("Renew an expired membership -> new expiryDate = today + plan duration")
    public void testRenewExpiredMembership() {
        LocalDate startDate = LocalDate.now().minusDays(40);
        LocalDate expiredExpiry = startDate.plusDays(30); // expired 10 days ago

        Membership membership = new Membership(savedMember, monthlyPlan, startDate, expiredExpiry);
        membership = membershipRepository.save(membership);
        assertEquals("EXPIRED", membership.getStatus());

        RenewMembershipRequest renewRequest = new RenewMembershipRequest(monthlyPlan.getPlanId());
        Membership renewed = membershipService.renewMembership(membership.getMembershipId(), renewRequest);

        LocalDate expectedExpiry = LocalDate.now().plusDays(30);
        assertEquals(expectedExpiry, renewed.getExpiryDate());
        assertEquals("ACTIVE", renewed.getStatus());
    }

    @Test
    @DisplayName("Renew membership with upgraded plan")
    public void testRenewWithPlanUpgrade() {
        LocalDate startDate = LocalDate.now();
        LocalDate oldExpiry = startDate.plusDays(30);

        Membership membership = new Membership(savedMember, monthlyPlan, startDate, oldExpiry);
        membership = membershipRepository.save(membership);

        RenewMembershipRequest renewRequest = new RenewMembershipRequest(quarterlyPlan.getPlanId());
        Membership renewed = membershipService.renewMembership(membership.getMembershipId(), renewRequest);

        assertEquals(quarterlyPlan.getPlanId(), renewed.getPlan().getPlanId());
        assertEquals(oldExpiry.plusDays(90), renewed.getExpiryDate());
    }
}
