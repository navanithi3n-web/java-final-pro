package com.gym.tracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class MembershipRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private LocalDate startDate;

    public MembershipRequest() {
    }

    public MembershipRequest(Long memberId, Long planId, LocalDate startDate) {
        this.memberId = memberId;
        this.planId = planId;
        this.startDate = startDate;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
