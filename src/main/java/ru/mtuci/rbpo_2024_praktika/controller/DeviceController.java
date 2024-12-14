package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.controller.dto.AddDeviceDTO;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.service.DeviceService;
import ru.mtuci.rbpo_2024_praktika.service.UserService;
import ru.mtuci.rbpo_2024_praktika.util.AuthUtil;

//TODO: 1. По заданию для всех сущностей должны поддерживаться все CRUD операции -
//+TODO: 2. Администратор не должен добавлять устройства вручную - теперь пользователь вручную добавляет свое устройство
//+TODO: 3. По вашей логике все устройства принадлежат администратору - Теперь любой пользователь создает устройства
@RequiredArgsConstructor
@RequestMapping("/device")
@RestController
public class DeviceController {

    private final DeviceService deviceService;
    private final UserService userService;
    private final AuthUtil authUtil;


    @PostMapping("/add")
    public ResponseEntity<String> addDevice(@RequestBody AddDeviceDTO addDeviceDTO) {
        try {
            User user = authUtil.getAuthenticatedUser();
            deviceService.addDevice(addDeviceDTO.getName(), user);
            return ResponseEntity.ok("Устройство добавлено");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

}
