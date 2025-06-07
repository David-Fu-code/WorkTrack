package com.david.worktrack.dto;

import com.david.worktrack.entity.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.management.relation.Role;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String displayName;
    private AppUserRole role;
}
