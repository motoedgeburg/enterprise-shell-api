package com.enterprise.shellapi.filter;

import com.enterprise.shellapi.config.JwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OktaJwtAuthenticationFilterTest {

    private OktaJwtAuthenticationFilter filter;
    private JwtConfig jwtConfig;
    private ObjectMapper objectMapper;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setIssuer("https://test.okta.com/oauth2/default");
        jwtConfig.setClientId("test-client-id");
        objectMapper = new ObjectMapper();
        filter = new OktaJwtAuthenticationFilter(jwtConfig, objectMapper);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_publicPaths() {
        assertThat(filter.shouldNotFilter(requestWithPath("/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(requestWithPath("/swagger-ui.html"))).isTrue();
        assertThat(filter.shouldNotFilter(requestWithPath("/swagger-ui/index.html"))).isTrue();
        assertThat(filter.shouldNotFilter(requestWithPath("/v3/api-docs"))).isTrue();
        assertThat(filter.shouldNotFilter(requestWithPath("/v3/api-docs/swagger-config"))).isTrue();
    }

    @Test
    void shouldFilter_apiPaths() {
        assertThat(filter.shouldNotFilter(requestWithPath("/api/records"))).isFalse();
        assertThat(filter.shouldNotFilter(requestWithPath("/api/lookups/departments"))).isFalse();
    }

    @Test
    void authBypass_setsLocalDevAuthentication() throws ServletException, IOException {
        setAuthBypass(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("local-dev@company.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void missingAuthHeader_returns401() throws ServletException, IOException {
        setAuthBypass(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void invalidBearerPrefix_returns401() throws ServletException, IOException {
        setAuthBypass(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void invalidToken_returns401() throws ServletException, IOException {
        setAuthBypass(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid or expired token");
        verify(filterChain, never()).doFilter(request, response);
    }

    private MockHttpServletRequest requestWithPath(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private void setAuthBypass(boolean value) {
        try {
            java.lang.reflect.Field field = OktaJwtAuthenticationFilter.class.getDeclaredField("authBypass");
            field.setAccessible(true);
            field.setBoolean(filter, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
