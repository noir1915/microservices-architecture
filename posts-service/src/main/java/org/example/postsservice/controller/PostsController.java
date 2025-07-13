package org.example.postsservice.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.postsservice.config.AppConfig;
import org.example.postsservice.dto.UserResponse;
import org.example.postsservice.service.PostsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

        String username = extractUsernameFromToken(request);

        Long userId = getUserIdByUsername(username);

        String content = (String) body.get("content");

        try {
            postsService.createPost(userId, content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public Long getUserIdByUsername(String username) {
        String url = "http://localhost:8082/api/users/username/" + username;
        try {
            UserResponse userResponse = restTemplate.getForObject(url, UserResponse.class);
            if (userResponse != null && userResponse.getId() != null) {
                return userResponse.getId();
            } else {
                throw new RuntimeException("Пользователь не найден");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Пользователь с логином " + username + " не найден");
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

            return claims.getSubject(); // возвращает имя пользователя
        } catch (JwtException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid token");
        }
    }
}