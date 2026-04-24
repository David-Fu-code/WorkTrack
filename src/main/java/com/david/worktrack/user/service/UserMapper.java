package com.david.worktrack.user.service;

import com.david.worktrack.user.dto.UserResponse;
import com.david.worktrack.user.entity.AppUser;

public class UserMapper {

    public static UserResponse toResponse(AppUser appUser) {

        return new UserResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getDisplayName(),
                appUser.getAppUserRole()
        );
    }
}
