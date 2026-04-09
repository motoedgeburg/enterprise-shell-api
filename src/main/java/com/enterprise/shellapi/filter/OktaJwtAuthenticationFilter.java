package com.enterprise.shellapi.filter;

import com.enterprise.shellapi.config.JwtConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class OktaJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/actuator/health",
            "/swagger-ui.html",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${auth.bypass:false}")
    private boolean authBypass;

    public OktaJwtAuthenticationFilter(JwtConfig jwtConfig, ObjectMapper objectMapper) {
        this.jwtConfig = jwtConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (authBypass) {
            log.debug("Auth bypass enabled — authenticating as local-dev@company.com");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("local-dev@company.com", null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            String userinfoUrl = jwtConfig.getIssuer() + "/v1/userinfo";
            HttpRequest userinfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(userinfoUrl))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> userinfoResponse = httpClient.send(userinfoRequest,
                    HttpResponse.BodyHandlers.ofString());

            if (userinfoResponse.statusCode() != 200) {
                log.warn("Okta userinfo returned status {}", userinfoResponse.statusCode());
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            JsonNode userInfo = objectMapper.readTree(userinfoResponse.body());
            String email = userInfo.has("email") ? userInfo.get("email").asText() : userInfo.get("sub").asText();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (userInfo.has("groups") && userInfo.get("groups").isArray()) {
                for (JsonNode group : userInfo.get("groups")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group.asText()));
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendUnauthorized(response, "Authentication service unavailable");
        } catch (Exception e) {
            log.error("Error validating token with Okta", e);
            sendUnauthorized(response, "Authentication failed");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "message", message,
                "status", 401,
                "timestamp", LocalDateTime.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
