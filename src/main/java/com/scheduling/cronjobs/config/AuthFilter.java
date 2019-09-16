package com.scheduling.cronjobs.config;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import com.scheduling.cronjobs.service.security.DbUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Component
@RequiredArgsConstructor
public class AuthFilter extends GenericFilterBean {

    private final DbUserDetailsService dbUserDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (Objects.nonNull(httpRequest.getUserPrincipal())) {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            String userId = httpRequest.getUserPrincipal().getName();

            UserDetails user;
            try {
                user = dbUserDetailsService.loadUserById(userId);
            } catch (UsernameNotFoundException e) {
                user = dbUserDetailsService.loadUserByUsername(userId);
            }
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    user.getAuthorities()
            );
            securityContext.setAuthentication(auth);
            httpRequest.getSession(true).setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
        }
        chain.doFilter(httpRequest, response);
    }

    @Override
    public void destroy() {
    }
}
