package com.david.worktrack.user.dto;

import com.david.worktrack.user.entity.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private AppUserRole role;
}
