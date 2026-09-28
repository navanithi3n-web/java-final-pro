package com.gym.tracker.repository;

import com.gym.tracker.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<Member> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}
