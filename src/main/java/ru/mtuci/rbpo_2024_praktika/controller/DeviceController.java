package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.service.DeviceService;
import ru.mtuci.rbpo_2024_praktika.service.UserService;

@RequiredArgsConstructor
@RequestMapping("/device")
@RestController
public class DeviceController {

    private final DeviceService deviceService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addDevice(@RequestBody AddDeviceDTO addDeviceDTO) {
        try {
            User user = getAuthenticatedUser();
            deviceService.addDevice(addDeviceDTO.getName(), user);
            return ResponseEntity.ok("Устройство добавлено");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = (String) authentication.getPrincipal();
            return userService.findByEmail(username).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        }
        return null;
    }
}
