package ru.mtuci.rbpo_2024_praktika.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo_2024_praktika.model.UserDetailsImpl;
import ru.mtuci.rbpo_2024_praktika.repository.UserRepository;

@RequiredArgsConstructor
@Service
public final class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserDetailsImpl.fromUser(userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found")));
    }

}
