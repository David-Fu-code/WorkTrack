package com.david.worktrack.admin;

import com.david.worktrack.user.dto.UserResponse;
import com.david.worktrack.user.entity.AppUser;
import com.david.worktrack.user.repository.AppUserRepository;
import com.david.worktrack.user.service.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppUserRepository repository;

    public List<UserResponse> getAllUsers() {

        List<AppUser> appUsers = repository.findAll();

        return appUsers.stream()
                .map(UserMapper::toResponse)
                .toList();
    }
}
