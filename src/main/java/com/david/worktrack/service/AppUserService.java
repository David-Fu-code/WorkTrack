package com.david.worktrack.service;

import com.david.worktrack.entity.AppUser;
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
      return appUserRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + email));
    }

    public void enableAppUser(String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User Not Found: " + email));

        appUser.setEnabled(true);
        appUser.setVerified(true);
        appUserRepository.save(appUser);
    }
}
