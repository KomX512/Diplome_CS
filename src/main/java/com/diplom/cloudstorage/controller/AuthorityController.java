package com.diplom.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.diplom.cloudstorage.dto.LoginRequest;
import com.diplom.cloudstorage.dto.LoginResponse;
import com.diplom.cloudstorage.exception.UnauthorizedException;
import com.diplom.cloudstorage.service.AuthService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthorityController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.login(), request.password());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (UnauthorizedException ex) {
            // Текст ошибки
            Map<String, String[]> errors = Map.of(
                    "login", new String[]{"Неверный логин или пароль"},
                    "password", new String[]{"Неверный логин или пароль"}
            );
            return ResponseEntity.status(401).body(errors);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
