package ru.mtuci.rbpo_2024_praktika.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.service.UserService;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserService userService;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String username = (String) authentication.getPrincipal();
        return userService.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
