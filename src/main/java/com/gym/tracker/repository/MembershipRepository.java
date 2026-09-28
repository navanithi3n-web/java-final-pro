package com.gym.tracker.repository;

import com.gym.tracker.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByMember_MemberId(Long memberId);
    
    Optional<Membership> findFirstByMember_MemberIdOrderByExpiryDateDesc(Long memberId);

    @Query("SELECT m FROM Membership m WHERE m.member.memberId = :memberId AND m.expiryDate >= :today ORDER BY m.expiryDate DESC")
    List<Membership> findActiveMembershipsForMember(@Param("memberId") Long memberId, @Param("today") LocalDate today);

    long countByExpiryDateGreaterThanEqual(LocalDate today);
}
