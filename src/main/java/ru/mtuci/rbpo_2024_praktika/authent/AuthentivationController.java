package ru.mtuci.rbpo_2024_praktika.authent;

import com.example.demo.configuration.JwtTokenProvider;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;


import java.util.stream.Collectors;


@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            String token = jwtProvider.createToken(
                    email,
                    authentication.getAuthorities().stream().collect(Collectors.toSet())
            );

            return ResponseEntity.ok(new LoginResponse(email, token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный пароль");
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<?> register(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String password) {
        try {
            userService.create(email, name, password);
            return ResponseEntity.ok("Регистрация выполнена!");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

}
