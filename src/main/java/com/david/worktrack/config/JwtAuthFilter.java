package com.david.worktrack.config;

import com.david.worktrack.entity.AppUser;
import com.david.worktrack.service.AppUserService;
import com.david.worktrack.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserService appUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/auth/register")
            || path.startsWith("/api/v1/auth/login")
            || path.startsWith("/api/v1/auth/confirm")) {
            filterChain.doFilter(request, response); // skip JWT check for public routes (No need to authenticated)
            return;
        }

        // This reads the HTTP header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // if no Authorization header or not Bearer -> skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7); // Remove "Bearer " (7 characters) → get the raw token.
        // Store username (email)
        username = jwtService.extractUsername(jwt);
        System.out.println(">>> JWT: " + jwt);
        System.out.println(">>> Username extracted: " + username);

        // if username not null and user not already authenticated
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load full user (roles, permissions, etc.)
            UserDetails userDetails = appUserService.loadUserByUsername(username);
            boolean isValid = jwtService.isTokenValid(jwt, userDetails.getUsername());
            System.out.println(">>> Is token valid? " + isValid);

            if (isValid) {
                System.out.println(">>> Token is VALID, setting Authentication");

                // Build authentication token / This tells Spring Security → "this user is now authenticated".
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Attach request details (IP address of client, More request metadata, etc.)
                // adds request metadata to the token — so later in the request chain, the app (or Spring Security) can access it.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set in SecurityContext / Now → the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        //Continue with next filter / After setting authentication, the request continues to the controller.
        filterChain.doFilter(request, response);
    }
}
