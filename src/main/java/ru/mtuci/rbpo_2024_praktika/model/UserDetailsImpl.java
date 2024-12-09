package ru.mtuci.rbpo_2024_praktika.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
    @Getter
    private final String username;
    @Getter
    private final String password;
    @Getter
    private final Set<GrantedAuthority> authorities;
    private final boolean isActive;

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public static UserDetails fromUser(User user) {
        return new UserDetailsImpl(
                user.getEmail(),
                user.getPassword(),
                user.getRole().getGrantedAuthorities(),
                true);
    }
}
