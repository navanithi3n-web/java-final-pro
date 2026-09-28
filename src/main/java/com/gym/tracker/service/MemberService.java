package com.gym.tracker.service;

import com.gym.tracker.dto.MemberRequest;
import com.gym.tracker.entity.Member;
import com.gym.tracker.exception.DuplicateResourceException;
import com.gym.tracker.exception.ResourceNotFoundException;
import com.gym.tracker.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(MemberRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Member with phone " + request.getPhone() + " already exists");
        }
        Member member = new Member();
        member.setName(request.getName().trim());
        member.setPhone(request.getPhone().trim());
        member.setEmail(request.getEmail().trim());
        member.setJoinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now());
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers(String search) {
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim();
            return memberRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query);
        }
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Member getMemberByPhone(String phone) {
        return memberRepository.findByPhone(phone.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with phone: " + phone));
    }

    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMemberById(id);
        String newPhone = request.getPhone().trim();
        if (!member.getPhone().equals(newPhone) && memberRepository.existsByPhone(newPhone)) {
            throw new DuplicateResourceException("Member with phone " + newPhone + " already exists");
        }
        member.setName(request.getName().trim());
        member.setPhone(newPhone);
        member.setEmail(request.getEmail().trim());
        if (request.getJoinDate() != null) {
            member.setJoinDate(request.getJoinDate());
        }
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public long countMembers() {
        return memberRepository.count();
    }
}
