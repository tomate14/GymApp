package com.fitexa.controler;

import com.fitexa.dtos.AuthChangePasswordRequestDto;
import com.fitexa.dtos.AuthChangePasswordResponseDto;
import com.fitexa.dtos.AuthLoginRequestDto;
import com.fitexa.dtos.AuthLoginResponseDto;
import com.fitexa.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Servicios de login y cambio de contrasena.")
public class AuthControler {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthLoginResponseDto login(@Valid @RequestBody AuthLoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public AuthChangePasswordResponseDto changePassword(@Valid @RequestBody AuthChangePasswordRequestDto request) {
        return authService.changePassword(request);
    }
}
