package com.gym.tracker.config;

import com.gym.tracker.entity.AppUser;
import com.gym.tracker.entity.Plan;
import com.gym.tracker.repository.AppUserRepository;
import com.gym.tracker.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final PlanRepository planRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(PlanRepository planRepository,
                      AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.planRepository  = planRepository;
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ── Seed Plans ────────────────────────────────────────────────────────
        if (planRepository.count() == 0) {
            planRepository.save(new Plan("Monthly",      30,  1000.0));
            planRepository.save(new Plan("Quarterly",    90,  2700.0));
            planRepository.save(new Plan("Yearly",      365,  9600.0));
            planRepository.save(new Plan("Weekly Trial",  7,   400.0));
            planRepository.save(new Plan("Half-Yearly", 180,  5000.0));
            log.info("Default gym plans initialized: Weekly Trial, Monthly, Quarterly, Half-Yearly, Yearly.");
        }

        // ── Seed Default Users ────────────────────────────────────────────────
        if (userRepository.count() == 0) {
            userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "Gym Admin", AppUser.Role.ADMIN));
            userRepository.save(new AppUser("staff", passwordEncoder.encode("staff123"), "Front Desk", AppUser.Role.STAFF));
            log.warn("==========================================================");
            log.warn("  Demo credentials seeded:  admin/admin123  &  staff/staff123");
            log.warn("  CHANGE THESE BEFORE GOING TO PRODUCTION!");
            log.warn("==========================================================");
        }
    }
}
