package com.hospital.reservation.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapConfig implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String name;

    public AdminBootstrapConfig(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.username:admin}") String username,
            @Value("${admin.password:change-me}") String password,
            @Value("${admin.name:최고 관리자}") String name
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.count() > 0) {
            return;
        }

        adminRepository.save(Admin.create(
                username,
                passwordEncoder.encode(password),
                name,
                AdminRole.SUPER_ADMIN
        ));
    }
}
