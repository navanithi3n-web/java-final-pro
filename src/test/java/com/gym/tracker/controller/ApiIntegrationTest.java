package com.gym.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.tracker.dto.CheckinRequest;
import com.gym.tracker.dto.MemberRequest;
import com.gym.tracker.dto.MembershipRequest;
import com.gym.tracker.dto.RenewMembershipRequest;
import com.gym.tracker.entity.Member;
import com.gym.tracker.entity.Membership;
import com.gym.tracker.entity.Plan;
import com.gym.tracker.repository.CheckinRepository;
import com.gym.tracker.repository.MemberRepository;
import com.gym.tracker.repository.MembershipRepository;
import com.gym.tracker.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    private Plan monthlyPlan;
    private Plan quarterlyPlan;

    @BeforeEach
    public void setup() {
        checkinRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();

        monthlyPlan = planRepository.findByPlanName("Monthly")
                .orElseGet(() -> planRepository.save(new Plan("Monthly", 30, 1000.0)));

        quarterlyPlan = planRepository.findByPlanName("Quarterly")
                .orElseGet(() -> planRepository.save(new Plan("Quarterly", 90, 2700.0)));
    }

    @Test
    @DisplayName("Register a valid member -> 201 Created")
    public void testRegisterValidMember() throws Exception {
        MemberRequest request = new MemberRequest("David Miller", "9812345678", "david@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.name").value("David Miller"))
                .andExpect(jsonPath("$.phone").value("9812345678"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }

    @Test
    @DisplayName("Register a member with an empty name -> 400 Bad Request")
    public void testRegisterMemberEmptyName() throws Exception {
        MemberRequest request = new MemberRequest("", "9812345679", "test@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Name is required")));
    }

    @Test
    @DisplayName("Duplicate phone -> 409 Conflict")
    public void testRegisterDuplicatePhone() throws Exception {
        memberRepository.save(new Member("First Member", "9998887776", "first@example.com"));

        MemberRequest duplicateRequest = new MemberRequest("Second Member", "9998887776", "second@example.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("Search a member by name and by phone")
    public void testSearchMemberByNameAndPhone() throws Exception {
        memberRepository.save(new Member("Bruce Wayne", "9112233445", "bruce@wayne.com"));
        memberRepository.save(new Member("Clark Kent", "9990001122", "clark@dailyplanet.com"));

        // Search by Name
        mockMvc.perform(get("/api/members?search=Wayne"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Bruce Wayne"));

        // Search by Phone
        mockMvc.perform(get("/api/members?search=999000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Clark Kent"));
    }

    @Test
    @DisplayName("Create a membership -> expiryDate is correct")
    public void testCreateMembership() throws Exception {
        Member member = memberRepository.save(new Member("Sarah Connor", "9876501234", "sarah@resistance.com"));

        MembershipRequest request = new MembershipRequest(member.getMemberId(), monthlyPlan.getPlanId(), LocalDate.now());

        mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").exists())
                .andExpect(jsonPath("$.memberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.planName").value("Monthly"))
                .andExpect(jsonPath("$.startDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.expiryDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Check-in with an active membership -> success and the record is saved")
    public void testCheckinActiveMembershipSuccess() throws Exception {
        Member member = memberRepository.save(new Member("Peter Parker", "9871122334", "peter@dailybugle.com"));
        membershipRepository.save(new Membership(member, monthlyPlan, LocalDate.now(), LocalDate.now().plusDays(30)));

        CheckinRequest request = new CheckinRequest(member.getMemberId(), null);

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkinId").exists())
                .andExpect(jsonPath("$.memberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.memberName").value("Peter Parker"))
                .andExpect(jsonPath("$.checkinTime").exists());

        // Verify check-in is also retrievable by member ID
        mockMvc.perform(get("/api/checkins/member/" + member.getMemberId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Check-in with an expired membership -> rejected with 400")
    public void testCheckinExpiredMembershipRejected() throws Exception {
        Member member = memberRepository.save(new Member("Tony Stark", "9870011223", "tony@stark.com"));
        membershipRepository.save(new Membership(member, monthlyPlan, LocalDate.now().minusDays(60), LocalDate.now().minusDays(30)));

        CheckinRequest request = new CheckinRequest(member.getMemberId(), null);

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Membership expired. Please renew."));
    }

    @Test
    @DisplayName("Check-in with an invalid member ID -> 404")
    public void testCheckinInvalidMemberId() throws Exception {
        CheckinRequest request = new CheckinRequest(888888L, null);

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Member not found with id: 888888")));
    }

    @Test
    @DisplayName("Renew an expired membership -> new expiryDate = today + plan duration")
    public void testRenewExpiredMembership() throws Exception {
        Member member = memberRepository.save(new Member("Natasha Romanoff", "9873344556", "natasha@shield.gov"));
        Membership expired = membershipRepository.save(new Membership(member, monthlyPlan, LocalDate.now().minusDays(50), LocalDate.now().minusDays(20)));

        RenewMembershipRequest request = new RenewMembershipRequest(monthlyPlan.getPlanId());

        mockMvc.perform(put("/api/memberships/" + expired.getMembershipId() + "/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiryDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Renew an active membership -> expiry extended from the old expiryDate")
    public void testRenewActiveMembership() throws Exception {
        Member member = memberRepository.save(new Member("Steve Rogers", "9874455667", "steve@shield.gov"));
        LocalDate oldExpiry = LocalDate.now().plusDays(15);
        Membership active = membershipRepository.save(new Membership(member, monthlyPlan, LocalDate.now().minusDays(15), oldExpiry));

        RenewMembershipRequest request = new RenewMembershipRequest(monthlyPlan.getPlanId());

        mockMvc.perform(put("/api/memberships/" + active.getMembershipId() + "/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiryDate").value(oldExpiry.plusDays(30).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
