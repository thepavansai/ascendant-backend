package com.ascendant.initiative.security;

import com.ascendant.initiative.model.User;
import com.ascendant.initiative.repository.UserRepository;
import com.ascendant.initiative.service.AdminAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.isValidAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null && AdminAuthService.ADMIN_ID.equals(userId) && "ADMIN".equals(role)) {
                user = User.builder()
                        .id(userId)
                        .name("Admin")
                        .email(adminEmail)
                        .passwordHash("")
                        .role(User.Role.ADMIN)
                        .build();
            }
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    user, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            // Invalid token — continue unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}
