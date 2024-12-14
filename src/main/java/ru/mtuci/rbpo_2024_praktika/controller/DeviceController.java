package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.controller.dto.AddDeviceDTO;
import ru.mtuci.rbpo_2024_praktika.controller.dto.RenameDeviceDTO;
import ru.mtuci.rbpo_2024_praktika.model.ApplicationRole;
import ru.mtuci.rbpo_2024_praktika.model.Device;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.service.DeviceService;
import ru.mtuci.rbpo_2024_praktika.service.UserService;
import ru.mtuci.rbpo_2024_praktika.util.AuthUtil;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/device")
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
    @GetMapping
    public ResponseEntity<List<Device>> getUserDevices() {
        User currentUser = authUtil.getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        // Предположим, что deviceService.findAllByUserId(...) вернёт все устройства пользователя.
        List<Device> devices = deviceService.findAllByUserId(currentUser.getId());
        return ResponseEntity.ok(devices);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id) {
        User currentUser = authUtil.getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("Пользователь не найден");
        }

        Optional<Device> deviceOpt = deviceService.findById(id);
        if (deviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Device device = deviceOpt.get();

        // Проверяем владельца (или роль admin), если требуется логика разграничения
        boolean isOwner = device.getUser().getId().equals(currentUser.getId());
        boolean isAdmin =  currentUser.getRole() == ApplicationRole.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("У вас нет доступа к данному устройству");
        }

        return ResponseEntity.ok(device);
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> renameDevice(
            @PathVariable Long id,
            @RequestBody RenameDeviceDTO renameDeviceDTO
    ) {
        try {
            User currentUser = authUtil.getAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Пользователь не найден или не авторизован");
            }

            Optional<Device> deviceOpt = deviceService.findById(id);
            if (deviceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Device device = deviceOpt.get();

            boolean isOwner = device.getUser().getId().equals(currentUser.getId());
            boolean isAdmin =  currentUser.getRole() == ApplicationRole.ADMIN;
            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(403).body("Вы не можете редактировать чужое устройство");
            }

            device.setName(renameDeviceDTO.getNewName());
            deviceService.save(device); // сохраняем изменения
            return ResponseEntity.ok("Устройство переименовано");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable Long id) {
        User currentUser = authUtil.getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("Пользователь не найден или не авторизован");
        }

        Optional<Device> deviceOpt = deviceService.findById(id);
        if (deviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Device device = deviceOpt.get();
        boolean isOwner = device.getUser().getId().equals(currentUser.getId());
        boolean isAdmin =  currentUser.getRole() == ApplicationRole.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("Вы не можете удалить чужое устройство");
        }

        deviceService.deleteById(id); // удаляем
        return ResponseEntity.ok("Устройство удалено");
    }
}
