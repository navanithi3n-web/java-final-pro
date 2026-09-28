package com.gym.tracker.dto;

import com.gym.tracker.entity.Checkin;
import java.time.LocalDateTime;

public class CheckinResponse {

    private Long checkinId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private LocalDateTime checkinTime;

    public CheckinResponse() {
    }

    public CheckinResponse(Checkin checkin) {
        this.checkinId = checkin.getCheckinId();
        if (checkin.getMember() != null) {
            this.memberId = checkin.getMember().getMemberId();
            this.memberName = checkin.getMember().getName();
            this.memberPhone = checkin.getMember().getPhone();
        }
        this.checkinTime = checkin.getCheckinTime();
    }

    public Long getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(Long checkinId) {
        this.checkinId = checkinId;
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

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }
}
