package com.projecthelpdesk.projecthelpdesk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.projecthelpdesk.projecthelpdesk.entity.Department;
import com.projecthelpdesk.projecthelpdesk.entity.ERole;
import com.projecthelpdesk.projecthelpdesk.entity.Role;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.repository.DepartmentRepository;
import com.projecthelpdesk.projecthelpdesk.repository.RoleRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, DepartmentRepository departmentRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed roles
        for (ERole eRole : ERole.values()) {
            if (roleRepository.findByRoleName(eRole).isEmpty()) {
                roleRepository.save(new Role(eRole));
                System.out.println("Created role: " + eRole);
            }
        }

        // Seed departments
        if (departmentRepository.count() == 0) {
            String[][] departments = {
                    { "IT Support", "Technical support and infrastructure issues" },
                    { "HR", "Human resources and employee services" },
                    { "Finance", "Billing, payments, and financial queries" },
                    { "Facilities", "Office maintenance and facility management" },
                    { "General", "General inquiries and other requests" }
            };
            for (String[] dept : departments) {
                Department d = new Department();
                d.setName(dept[0]);
                d.setDescription(dept[1]);
                departmentRepository.save(d);
                System.out.println("Created department: " + dept[0]);
            }
        }

        // Seed default admin user
        if (userRepository.findByEmail("admin@helpdesk.com").isEmpty()) {
            Role adminRole = roleRepository.findByRoleName(ERole.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            User admin = new User();
            admin.setFullName("Admin");
            admin.setEmail("admin@helpdesk.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println("Created default admin: admin@helpdesk.com / admin123");
        }
    }
}
