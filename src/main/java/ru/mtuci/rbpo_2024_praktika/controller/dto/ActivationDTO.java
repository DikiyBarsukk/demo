package ru.mtuci.rbpo_2024_praktika.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ActivationDTO {
    String mac;
    String key;
}
