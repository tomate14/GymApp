package com.fitexa.security;

import com.fitexa.entities.UserEntity;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class FitexaUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String userType;
    private final Collection<? extends GrantedAuthority> authorities;

    public static FitexaUserDetails from(UserEntity entity) {
        return new FitexaUserDetails(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getFirstName() + " " + entity.getLastName(),
            entity.getUserType().getValue(),
            List.of(new SimpleGrantedAuthority("ROLE_" + entity.getUserType().name()))
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
