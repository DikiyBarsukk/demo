package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.model.LicenseType;
import ru.mtuci.rbpo_2024_praktika.service.LicenseService;
import ru.mtuci.rbpo_2024_praktika.service.LicenseTypeService;


@RequiredArgsConstructor
@RestController
@RequestMapping("/license-type")
public class LicenseTypeController {

    private final LicenseTypeService licenseTypeService;
    private final LicenseService licenseService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addLicenseType(@RequestBody LicenseType licenseType) {
        try {
            LicenseType createdLicenseType = licenseTypeService.addLicenseType(licenseType);
            return ResponseEntity.ok("Тип лицензии создан с ID: " + createdLicenseType.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при создании типа лицензии: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeLicenseType(@PathVariable Long id) {
        try {
            if (licenseService.existsByLicenseTypeId(id)) {
                return ResponseEntity.badRequest().body("Невозможно удалить LicenseType.");
            }
            licenseTypeService.deleteById(id);
            return ResponseEntity.ok("LicenseType успешно удалён.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
        }
    }


}
