package com.david.worktrack.security.jwt;

import com.david.worktrack.user.service.AppUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;



@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Service responsible for JWT operations (extract username, validate token, etc.)
    private final JwtService jwtService;

    // Service used to load user details from database
    private final AppUserService appUserService;

    // Logger for debugging authentication flow
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Constructor injection of required dependencies
    public JwtAuthFilter(JwtService jwtService, AppUserService appUserService){
        this.jwtService = jwtService;
        this.appUserService = appUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extract Authorization header for request
        final String authHeader = request.getHeader("Authorization");

        // if no Authorization header or not Bearer -> skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract raw JWT token (Remove "Bearer " (7 characters))
        final String jwt = authHeader.substring(7);

        // Extract username from JWT payload
        final String username = jwtService.extractUsername(jwt);

        log.debug("JWT extracted: {}", jwt);
        log.debug("Username extracted: {}", username);

        // Proceed ony if username exists and user is not already authenticated
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate token before trusting it
            if (jwtService.isTokenValid(jwt, username)) {

                // Load full user details (roles, authorities, etc.)
                UserDetails userDetails = appUserService.loadUserByUsername(username);

                // Create authentication token for Spring Security context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request metadata (IP, session info, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set authenticated user in Security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authentication set for user: {}", username);
            }
        }

        // Continue filter chain execution
        filterChain.doFilter(request, response);
    }
}