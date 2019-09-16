package com.scheduling.cronjobs.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ODataBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Value("${odata.authentication.header.name}")
    private String odataHeaderName;
    @Value("${odata.authentication.header.value}")
    private String odataHeaderValue;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.addHeader(odataHeaderName, odataHeaderValue);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        log.error("HTTP Status 401 - {}", authException.getMessage());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("REALM");
        super.afterPropertiesSet();
    }
}
