package org.example.postsservice.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.postsservice.dto.UserResponse;
import org.example.postsservice.service.PostsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    @Value("${api.secret}")
    private String secret;

    private final RestTemplate restTemplate;

    private final PostsService postsService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        log.info("Received body: {}", body);
        String login = extractUsernameFromToken(request);
        log.info("Extracted login from token: {}", login);
        String token = extractTokenFromRequest(request);
        Long userId = getUserIdByLogin(login, token);
        log.info("Получен userId: {}", userId);
        if (userId == null) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }

        String content = (String) body.get("content");

        try {
            postsService.createPost(userId, content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    public Long getUserIdByLogin(String login, String token) {
        String url = "http://localhost:8082/auth/login/" + login;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserResponse.class
            );
            UserResponse userResponse = response.getBody();
            if (userResponse != null && userResponse.getId() != null) {
                return userResponse.getId();
            } else {
                throw new RuntimeException("Пользователь не найден");
            }
        } catch (HttpClientErrorException.Forbidden e) {
            throw new RuntimeException("Доступ запрещён при получении пользователя");
        }
        catch (HttpClientErrorException e) {
            log.error("Error fetching user by login: {}. Status: {}, Response: {}", login, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка при получении пользователя");
        }
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid token");
        }
    }
}