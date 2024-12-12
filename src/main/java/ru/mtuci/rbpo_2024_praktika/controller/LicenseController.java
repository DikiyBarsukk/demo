package ru.mtuci.rbpo_2024_praktika.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.controller.dto.ActivationDTO;
import ru.mtuci.rbpo_2024_praktika.controller.dto.AddLicenseDTO;
import ru.mtuci.rbpo_2024_praktika.controller.dto.RenewDTO;
import ru.mtuci.rbpo_2024_praktika.model.License;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.service.LicenseService;
import ru.mtuci.rbpo_2024_praktika.service.UserService;
import ru.mtuci.rbpo_2024_praktika.ticket.Ticket;

import java.util.NoSuchElementException;

//TODO: 1. Между классами есть дублирование кода

@RequiredArgsConstructor
@RequestMapping("/licenses")
@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class LicenseController {
    private final LicenseService licenseService;
    private final UserService userService;

    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(
            @RequestBody ActivationDTO activationDTO) {
        try {
            User currentUser = getAuthenticatedUser();
            Ticket ticket = licenseService.processActivation(activationDTO.getMac(), activationDTO.getKey(), currentUser);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка: " + e.getMessage());
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> add(
            @RequestBody AddLicenseDTO addLicenseDTO) {
        try {
            License createdLicense = licenseService.createLicense(addLicenseDTO.getProductId(), addLicenseDTO.getOwnerId(), addLicenseDTO.getLicenseTypeId(), addLicenseDTO.getDeviceCount());
            return ResponseEntity.ok("Лицензия создана с ID: " + createdLicense.getId());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании лицензии: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/renew")
    public ResponseEntity<?> renewLicense(
            @RequestBody RenewDTO renewDTO) {
        try {
            User authenticatedUser = getAuthenticatedUser();
            Ticket ticket = licenseService.renewLicense(renewDTO.getLicenseKey(), renewDTO.getMacAddress(), authenticatedUser);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeLicense(@PathVariable Long id) {
        try {
            License license = licenseService.findById(id);
            if (license == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лицензия не найдена.");
            }
            if (license.getActivationDate() != null) {
                return ResponseEntity.badRequest().body("Невозможно удалить активированную лицензию.");
            }
            licenseService.deleteById(id);
            return ResponseEntity.ok("Лицензия успешно удалена.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
        }
    }

    @PatchMapping("/block/{licenseId}")
    public ResponseEntity<String> changeLicenseStatus(
            @PathVariable Long licenseId,
            @RequestParam boolean isBlocked) {
        try {
            User authenticatedUser = getAuthenticatedUser();
            licenseService.changeLicenseStatus(licenseId, isBlocked, authenticatedUser);
            return ResponseEntity.ok("Статус лицензии успешно обновлен.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Лицензия с таким ID не найдена");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при изменении статуса лицензии: " + e.getMessage());
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




