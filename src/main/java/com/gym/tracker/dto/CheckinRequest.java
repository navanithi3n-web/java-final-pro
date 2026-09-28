package com.gym.tracker.dto;

public class CheckinRequest {

    private Long memberId;
    private String phone;

    public CheckinRequest() {
    }

    public CheckinRequest(Long memberId) {
        this.memberId = memberId;
    }

    public CheckinRequest(String phone) {
        this.phone = phone;
    }

    public CheckinRequest(Long memberId, String phone) {
        this.memberId = memberId;
        this.phone = phone;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
