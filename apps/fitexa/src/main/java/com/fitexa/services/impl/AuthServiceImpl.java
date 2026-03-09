package com.fitexa.services.impl;

import com.fitexa.dtos.AuthChangePasswordRequestDto;
import com.fitexa.dtos.AuthChangePasswordResponseDto;
import com.fitexa.dtos.AuthLoginRequestDto;
import com.fitexa.dtos.AuthLoginResponseDto;
import com.fitexa.entities.UserEntity;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.InvalidCredentialsException;
import com.fitexa.repository.UserRepository;
import com.fitexa.security.FitexaUserDetails;
import com.fitexa.security.JwtService;
import com.fitexa.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthLoginResponseDto login(AuthLoginRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Credenciales invalidas.");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas."));
        FitexaUserDetails userDetails = FitexaUserDetails.from(user);

        return AuthLoginResponseDto.builder()
            .token(jwtService.generateToken(userDetails))
            .tokenType("Bearer")
            .expiresInSeconds(jwtService.getExpirationSeconds())
            .userId(user.getId())
            .fullName(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .userType(user.getUserType().getValue())
            .mustChangePassword(user.isMustChangePassword())
            .build();
    }

    @Override
    @Transactional
    public AuthChangePasswordResponseDto changePassword(AuthChangePasswordRequestDto request) {
        UserEntity user = findAuthenticatedUser();
        String currentPassword = request.getCurrentPassword().trim();
        String newPassword = request.getNewPassword().trim();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("La contrasena actual es invalida.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessValidationException("La nueva contrasena debe ser distinta a la actual.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return AuthChangePasswordResponseDto.builder()
            .message("Contrasena actualizada correctamente.")
            .build();
    }

    private UserEntity findAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("Sesion no valida.");
        }

        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new InvalidCredentialsException("Sesion no valida."));
    }
}
