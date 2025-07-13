package org.example.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginDto;
import org.example.authservice.dto.UserDto;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
        if(userRepository.findByLogin(userDto.getLogin()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login already exists");
        }
        User user = new User();
        user.setLogin(userDto.getLogin());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        userRepository.save(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginDto loginDto) {
        Optional<User> userOpt = userRepository.findByLogin(loginDto.getLogin());
        if(userOpt.isEmpty() || !passwordEncoder.matches(loginDto.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(userOpt.get().getLogin());

        return ResponseEntity.ok(Map.of("token", token));
    }


    @GetMapping("/login/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable("login")  String login) {
       return userRepository.findByLogin(login)
                .map(UserDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
