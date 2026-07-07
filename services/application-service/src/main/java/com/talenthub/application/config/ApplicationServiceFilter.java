package com.talenthub.application.config;

import com.talenthub.application.api.dto.UserDetail;
import com.talenthub.application.utils.Constraints;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ApplicationServiceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(Constraints.HEADER_CORRELATION_ID);
        String userId = request.getHeader(Constraints.HEADER_USER_ID);
        String email = request.getHeader(Constraints.HEADER_EMAIL);
        String name = request.getHeader(Constraints.HEADER_NAME);
        String username = request.getHeader(Constraints.HEADER_USERNAME);

        // How to store security context
        UserDetail userDetail = new UserDetail(userId, email, username, name);

        String rolesHeader = request.getHeader(Constraints.HEADER_ROLE_NAME);
        List<String> roles = (rolesHeader != null && !rolesHeader.isEmpty())
                ? Arrays.asList(rolesHeader.split(","))
                : Collections.emptyList();
        // Gán quyền hạn có tiền tố ROLE_ cho Spring Security
        // CANDIDATE -> SimpleGrantedAuthority(ROLE_CANDIDATE)
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .toList();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetail, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);

        filterChain.doFilter(request, response);
    }
}
