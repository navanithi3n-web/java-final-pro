package com.gym.tracker.dto;

public class RenewMembershipRequest {

    private Long planId;

    public RenewMembershipRequest() {
    }

    public RenewMembershipRequest(Long planId) {
        this.planId = planId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }
}
