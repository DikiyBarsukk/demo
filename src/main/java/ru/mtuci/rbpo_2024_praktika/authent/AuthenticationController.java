package ru.mtuci.rbpo_2024_praktika.authent;

import ru.mtuci.rbpo_2024_praktika.configuration.JwtTokenProvider;
import ru.mtuci.rbpo_2024_praktika.service.UserService;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            String token = jwtProvider.createToken(
                    loginRequest.getEmail(),
                    authentication.getAuthorities().stream().collect(Collectors.toSet())
            );

            return ResponseEntity.ok(new LoginResponse(loginRequest.getEmail(), token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный пароль");
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest registerRequest) {
        try {
            userService.create(registerRequest.getEmail(), registerRequest.getName(), registerRequest.getPassword());
            return ResponseEntity.ok("Регистрация выполнена!");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

}