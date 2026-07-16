package com.hospital.reservation.admin;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));

        String[] roles = admin.getRole() == AdminRole.SUPER_ADMIN
                ? new String[]{"ADMIN", "SUPER_ADMIN"}
                : new String[]{"ADMIN"};

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash())
                .roles(roles)
                .disabled(!admin.isActive())
                .build();
    }
}
