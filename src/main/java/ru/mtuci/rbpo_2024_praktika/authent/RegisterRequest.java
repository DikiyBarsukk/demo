package ru.mtuci.rbpo_2024_praktika.authent;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
}

