package com.gym.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status;

    public Membership() {
    }

    public Membership(Member member, Plan plan, LocalDate startDate, LocalDate expiryDate) {
        this.member = member;
        this.plan = plan;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        updateStatus();
    }

    public void updateStatus() {
        if (this.expiryDate != null && !LocalDate.now().isAfter(this.expiryDate)) {
            this.status = "ACTIVE";
        } else {
            this.status = "EXPIRED";
        }
    }

    @PostLoad
    public void postLoad() {
        updateStatus();
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        updateStatus();
    }

    public boolean isActive() {
        return this.expiryDate != null && !LocalDate.now().isAfter(this.expiryDate);
    }

    // Getters and Setters
    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
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
        updateStatus();
    }

    public String getStatus() {
        updateStatus();
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
