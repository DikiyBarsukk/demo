package ru.mtuci.rbpo_2024_praktika.authent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {
    private String email;
    private String token;
}
