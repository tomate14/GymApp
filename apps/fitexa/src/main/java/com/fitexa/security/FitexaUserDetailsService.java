package com.fitexa.security;

import com.fitexa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FitexaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(username)
            .map(FitexaUserDetails::from)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    @Transactional(readOnly = true)
    public FitexaUserDetails loadByEmailAndClientId(String email, Long clientId) {
        if (clientId == null) {
            throw new UsernameNotFoundException("Cliente no valido.");
        }
        return userRepository.findByEmailIgnoreCaseAndClientId(email, clientId)
            .map(FitexaUserDetails::from)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }
}
