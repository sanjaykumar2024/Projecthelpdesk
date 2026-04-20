package com.projecthelpdesk.projecthelpdesk;

import javax.sql.DataSource;

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
    private final DataSource dataSource;

    public DataInitializer(RoleRepository roleRepository, DepartmentRepository departmentRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder, DataSource dataSource) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        // Fix: Allow password column to be NULL (for Google Sign-In users)
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL");
            System.out.println("Schema fix: password column set to nullable.");
        } catch (Exception e) {
            System.out.println("Schema fix skipped: " + e.getMessage());
        }

        // Fix: Expand role_name ENUM to include HOD
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE roles MODIFY COLUMN role_name ENUM('USER','AGENT','ADMIN','HOD') NOT NULL");
            System.out.println("Schema fix: role_name column updated to include HOD.");
        } catch (Exception e) {
            System.out.println("Schema fix (role_name) skipped: " + e.getMessage());
        }

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
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Created default admin: admin@helpdesk.com / admin123");
        } else {
            // Ensure existing admin is enabled (migration fix)
            userRepository.findByEmail("admin@helpdesk.com").ifPresent(admin -> {
                if (!admin.isEnabled()) {
                    admin.setEnabled(true);
                    userRepository.save(admin);
                    System.out.println("Enabled existing admin user.");
                }
            });
        }
    }
}
