package com.david.worktrack.user.service;

import com.david.worktrack.user.dto.ChangePasswordRequest;
import com.david.worktrack.user.dto.UpdateProfileRequest;
import com.david.worktrack.user.dto.UserResponse;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.user.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final AppUserService appUserService;
    private final AppUserRepository repository;


    public UserResponse getUser(AppUser appUser) {

        return  new UserResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getDisplayName(),
                appUser.getAppUserRole());

    }

    public void updateProfileName(UpdateProfileRequest request, AppUser appUser) {

        appUser.setDisplayName(request.getName());

        repository.save(appUser);
    }

    public void changePassword(ChangePasswordRequest request, AppUser appUser) {

        appUserService.validatePassword(request.getCurrentPassword(), appUser.getPassword());

        appUserService.updatePassword(appUser, request.getNewPassword());

    }


}
