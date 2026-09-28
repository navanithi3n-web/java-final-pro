package com.gym.tracker.service;

import com.gym.tracker.dto.MembershipRequest;
import com.gym.tracker.dto.MembershipResponse;
import com.gym.tracker.dto.RenewMembershipRequest;
import com.gym.tracker.entity.Member;
import com.gym.tracker.entity.Membership;
import com.gym.tracker.entity.Plan;
import com.gym.tracker.exception.ResourceNotFoundException;
import com.gym.tracker.repository.MemberRepository;
import com.gym.tracker.repository.MembershipRepository;
import com.gym.tracker.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;

    public MembershipService(MembershipRepository membershipRepository,
                             MemberRepository memberRepository,
                             PlanRepository planRepository) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    public Membership createMembership(MembershipRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + request.getPlanId()));

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate expiryDate = startDate.plusDays(plan.getDurationInDays());

        Membership membership = new Membership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(startDate);
        membership.setExpiryDate(expiryDate);
        membership.updateStatus();

        return membershipRepository.save(membership);
    }

    public Membership renewMembership(Long membershipId, RenewMembershipRequest request) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + membershipId));

        Plan planToUse = membership.getPlan();
        if (request != null && request.getPlanId() != null) {
            planToUse = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + request.getPlanId()));
            membership.setPlan(planToUse);
        }

        LocalDate today = LocalDate.now();
        LocalDate currentExpiry = membership.getExpiryDate();
        LocalDate newExpiryDate;

        // Rule 5: If still active (today <= expiryDate), extend from current expiry. If expired, extend from today.
        if (currentExpiry != null && !today.isAfter(currentExpiry)) {
            newExpiryDate = currentExpiry.plusDays(planToUse.getDurationInDays());
        } else {
            newExpiryDate = today.plusDays(planToUse.getDurationInDays());
        }

        membership.setExpiryDate(newExpiryDate);
        membership.updateStatus();

        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<Membership> getAllMemberships() {
        List<Membership> memberships = membershipRepository.findAll();
        memberships.forEach(Membership::updateStatus);
        return memberships;
    }

    @Transactional(readOnly = true)
    public Membership getMembershipById(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));
        membership.updateStatus();
        return membership;
    }

    @Transactional(readOnly = true)
    public List<Membership> getMembershipsByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        List<Membership> memberships = membershipRepository.findByMember_MemberId(memberId);
        memberships.forEach(Membership::updateStatus);
        return memberships;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveMembership(Long memberId) {
        List<Membership> activeList = membershipRepository.findActiveMembershipsForMember(memberId, LocalDate.now());
        return !activeList.isEmpty();
    }

    @Transactional(readOnly = true)
    public long countActiveMemberships() {
        return membershipRepository.countByExpiryDateGreaterThanEqual(LocalDate.now());
    }
}
