package com.david.worktrack.service;

import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.InvalidTokenException;
import com.david.worktrack.exception.ResourceNotFoundException;
import com.david.worktrack.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

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
}
