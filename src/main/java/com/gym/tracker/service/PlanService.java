package com.gym.tracker.service;

import com.gym.tracker.dto.PlanRequest;
import com.gym.tracker.entity.Plan;
import com.gym.tracker.exception.ResourceNotFoundException;
import com.gym.tracker.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Plan createPlan(PlanRequest request) {
        Plan plan = new Plan();
        plan.setPlanName(request.getPlanName().trim());
        plan.setDurationInDays(request.getDurationInDays());
        plan.setPrice(request.getPrice());
        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    public Plan updatePlan(Long id, PlanRequest request) {
        Plan plan = getPlanById(id);
        plan.setPlanName(request.getPlanName().trim());
        plan.setDurationInDays(request.getDurationInDays());
        plan.setPrice(request.getPrice());
        return planRepository.save(plan);
    }
}
