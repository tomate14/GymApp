package com.fitexa.services.impl;

import com.fitexa.entities.ClientEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.exeptions.InvalidCredentialsException;
import com.fitexa.repository.UserRepository;
import com.fitexa.security.FitexaUserDetails;
import com.fitexa.services.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEntity getCurrentUser() {
        FitexaUserDetails currentUser = currentUserDetails();
        return userRepository.findByIdAndClientId(currentUser.getId(), currentUser.getClientId())
            .orElseThrow(() -> new InvalidCredentialsException("Sesion no valida."));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientEntity getCurrentClient() {
        return getCurrentUser().getClient();
    }

    @Override
    public Long getCurrentClientId() {
        return currentUserDetails().getClientId();
    }

    private FitexaUserDetails currentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("Sesion no valida.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof FitexaUserDetails userDetails) {
            return userDetails;
        }
        throw new InvalidCredentialsException("Sesion no valida.");
    }
}
