package com.david.worktrack.user.service;

import com.david.worktrack.auth.dto.RegisterRequest;
import com.david.worktrack.user.dto.ChangePasswordRequest;
import com.david.worktrack.user.dto.UpdateProfileRequest;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.user.entity.AppUserRole;
import com.david.worktrack.common.exception.BusinessException;
import com.david.worktrack.common.exception.ResourceNotFoundException;
import com.david.worktrack.user.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final AppUserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void enableAppUser(String email) {
        AppUser appUser = getUserByEmailOrThrow(email);

        appUser.setEnabled(true);
        appUser.setVerified(true);
        repository.save(appUser);
    }

    // Returns user or throws exception if not found (required use cases like authentication)
    public AppUser getUserByEmailOrThrow(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public void checkEmailExistsOrThrow(String email) {

        if (repository.findByEmail(email).isPresent()) {
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

        return repository.save(appUser);
    }

    // Returns Optional user, empty if not found (optional flows like forgot password)
    public Optional<AppUser> getUserByEmail(String email) {

        return repository.findByEmail(email);
    }

    public void updatePassword(AppUser appUser, String newPassword) {

        appUser.setPassword(encodePassword(newPassword));

        repository.save(appUser);
    }

    public void validatePassword(String rawPassword, String encodedPassword) {

        if (!bCryptPasswordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException("Invalid credentials");
        }
    }

    public String encodePassword(String password) {

        return bCryptPasswordEncoder.encode(password);
    }

    public void updateProfileName(UpdateProfileRequest request, AppUser appUser) {

        appUser.setDisplayName(request.getName());

        repository.save(appUser);
    }

    public void changePassword(ChangePasswordRequest request, AppUser appUser) {

        validatePassword(request.getCurrentPassword(), appUser.getPassword());

        updatePassword(appUser, request.getNewPassword());

    }

}
