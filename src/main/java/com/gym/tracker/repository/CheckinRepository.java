package com.gym.tracker.repository;

import com.gym.tracker.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {
    List<Checkin> findByMember_MemberIdOrderByCheckinTimeDesc(Long memberId);

    List<Checkin> findByCheckinTimeBetweenOrderByCheckinTimeDesc(LocalDateTime start, LocalDateTime end);

    long countByCheckinTimeBetween(LocalDateTime start, LocalDateTime end);
}
