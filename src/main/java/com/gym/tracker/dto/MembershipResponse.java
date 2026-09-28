package com.gym.tracker.dto;

import com.gym.tracker.entity.Membership;
import java.time.LocalDate;

public class MembershipResponse {

    private Long membershipId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private Long planId;
    private String planName;
    private Integer durationInDays;
    private Double price;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private String status;

    public MembershipResponse() {
    }

    public MembershipResponse(Membership m) {
        this.membershipId = m.getMembershipId();
        if (m.getMember() != null) {
            this.memberId = m.getMember().getMemberId();
            this.memberName = m.getMember().getName();
            this.memberPhone = m.getMember().getPhone();
        }
        if (m.getPlan() != null) {
            this.planId = m.getPlan().getPlanId();
            this.planName = m.getPlan().getPlanName();
            this.durationInDays = m.getPlan().getDurationInDays();
            this.price = m.getPlan().getPrice();
        }
        this.startDate = m.getStartDate();
        this.expiryDate = m.getExpiryDate();
        this.status = m.getStatus();
    }

    // Getters and Setters
    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(Integer durationInDays) {
        this.durationInDays = durationInDays;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
