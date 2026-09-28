package com.gym.tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    public PlanRequest() {
    }

    public PlanRequest(String planName, Integer durationInDays, Double price) {
        this.planName = planName;
        this.durationInDays = durationInDays;
        this.price = price;
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
}
