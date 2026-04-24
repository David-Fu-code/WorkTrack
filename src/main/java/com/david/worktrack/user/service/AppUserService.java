package com.david.worktrack.user.service;

import com.david.worktrack.auth.dto.RegisterRequest;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.user.entity.AppUserRole;
import com.david.worktrack.common.exception.BusinessException;
import com.david.worktrack.common.exception.ResourceNotFoundException;
import com.david.worktrack.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Called automatically by Spring Security during authentication (login process)
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // If found, the user is returned as UserDetails for authentication
    }

    public void enableAppUser(String email) {
        AppUser appUser = getUserByEmailOrThrow(email);

        appUser.setEnabled(true);
        appUser.setVerified(true);
        appUserRepository.save(appUser);
    }

    public AppUser getUserByEmailOrThrow(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public void checkEmailExistsOrThrow(String email) {

        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email already registered");
        }
    }

    public AppUser createAndSaveUser(RegisterRequest request) {

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .password(encodePassword(request.getPassword()))
                .displayName(request.getDisplayName())
                .appUserRole(AppUserRole.USER)
                .enabled(false)
                .verified(false)
                .locked(false)
                .build();

        return appUserRepository.save(appUser);
    }

    public Optional<AppUser> getUserByEmail(String email) {

        return appUserRepository.findByEmail(email);
    }

    public void updatePassword(AppUser appUser, String newPassword) {

        appUser.setPassword(encodePassword(newPassword));

        appUserRepository.save(appUser);
    }

    public void validatePassword(String rawPassword, String encodedPassword) {

        if (!bCryptPasswordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException("Invalid credentials");
        }
    }

    public String encodePassword(String password) {

        return bCryptPasswordEncoder.encode(password);
    }

}
