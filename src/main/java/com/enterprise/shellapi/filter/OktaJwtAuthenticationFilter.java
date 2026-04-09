package com.enterprise.shellapi.filter;

import com.enterprise.shellapi.config.JwtConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile Instant keyCacheExpiry = Instant.MIN;

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
            Claims claims = validateToken(token);

            String email = claims.containsKey("email") ? claims.get("email", String.class) : claims.getSubject();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Object groups = claims.get("groups");
            if (groups instanceof List<?> groupList) {
                for (Object group : groupList) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group));
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error validating JWT", e);
            sendUnauthorized(response, "Invalid or expired token");
        }
    }

    private Claims validateToken(String token) throws Exception {
        refreshKeysIfNeeded();

        return Jwts.parser()
                .requireIssuer(jwtConfig.getIssuer())
                .keyLocator(header -> {
                    String kid = (String) header.get("kid");
                    PublicKey key = keyCache.get(kid);
                    if (key == null) {
                        throw new io.jsonwebtoken.security.SecurityException("Unknown signing key: " + kid);
                    }
                    return key;
                })
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private synchronized void refreshKeysIfNeeded() throws Exception {
        if (Instant.now().isBefore(keyCacheExpiry)) return;

        String jwksUrl = jwtConfig.getIssuer() + "/v1/keys";
        HttpRequest jwksRequest = HttpRequest.newBuilder()
                .uri(URI.create(jwksUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> jwksResponse = httpClient.send(jwksRequest, HttpResponse.BodyHandlers.ofString());

        if (jwksResponse.statusCode() != 200) {
            throw new IllegalStateException("Failed to fetch JWKS from " + jwksUrl + ": " + jwksResponse.statusCode());
        }

        JsonNode jwks = objectMapper.readTree(jwksResponse.body());
        Map<String, PublicKey> newKeys = new ConcurrentHashMap<>();

        for (JsonNode key : jwks.get("keys")) {
            if ("RSA".equals(key.get("kty").asText()) && "sig".equals(key.get("use").asText())) {
                String kid = key.get("kid").asText();
                BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("n").asText()));
                BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("e").asText()));
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
                newKeys.put(kid, publicKey);
            }
        }

        keyCache.clear();
        keyCache.putAll(newKeys);
        keyCacheExpiry = Instant.now().plus(Duration.ofHours(1));
        log.info("Refreshed JWKS cache — {} keys loaded", newKeys.size());
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
