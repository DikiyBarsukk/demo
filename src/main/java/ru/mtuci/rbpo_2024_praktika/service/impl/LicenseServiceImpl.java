package ru.mtuci.rbpo_2024_praktika.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.rbpo_2024_praktika.model.*;
import ru.mtuci.rbpo_2024_praktika.repository.DeviceLicenseRepository;
import ru.mtuci.rbpo_2024_praktika.repository.DeviceRepository;
import ru.mtuci.rbpo_2024_praktika.repository.LicenseRepository;
import ru.mtuci.rbpo_2024_praktika.service.*;
import ru.mtuci.rbpo_2024_praktika.ticket.Ticket;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

//TODO: 1. Плохой способ борьбы с коллизиями кодов активации - ?Сделал по другому, теперь будет 100 попыток создать УНИКАЛЬНЫЙ код
//TODO: 2. По вашей логике, пользователь не может активировать лицензию с нового устройства - может, если перед активацией юзер зарегестрирует свое устройство, и потом на активацию отправит его мак адрес
//TODO: 3. Где лицензии проставляется дата первой активации? - Потерял - добавил в validateAndRetrieveLicense
//TODO: 4. Возвращать список тикетов не нужно, вернуть только один тикет - ?

@RequiredArgsConstructor
@Service
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final ProductService productService;
    private final UserService userService;
    private final DeviceService deviceService;
    private final LicenseTypeService licenseTypeService;
    private final LicenseHistoryService licenseHistoryService;
    private final DeviceLicenseService deviceLicenseService;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final DeviceRepository deviceRepository;


    @Override
    public void add(License license) {
        licenseRepository.save(license);
    }

    @Override
    public License createLicense(Long productId, Long ownerId, Long licenseTypeId, Integer deviceCount) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return null; // Продукт не найден
        }

        User user = userService.getById(ownerId);
        if (user == null) {
            return null; // Пользователь не найден
        }

        LicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId);
        if (licenseType == null) {
            return null; // Тип лицензии не найден
        }

        License license = new License();
        license.setProduct(product);
        license.setOwner(user);
        license.setLicenseType(licenseType);
        license.setDeviceCount(deviceCount);

        String activationCode = null;
        for (int i = 0; i < 100; i++) {
            String candidateCode = UUID.randomUUID().toString();
            if (!licenseRepository.existsByKey(candidateCode)) {
                activationCode = candidateCode;
                break;
            }
        }

        license.setKey(activationCode);
        licenseRepository.save(license);
        licenseHistoryService.recordLicenseChange(license, user, "Создана", "Лицензия успешно создана");
        return license;
    }

    @Override
    public Ticket processActivation(String macAddress, String licenseKey, User user) {
        Device device = deviceRepository.findByMac(macAddress).orElse(null);
        if (device == null) {
            return null; // Устройство не найдено
        }

        License license = validateAndRetrieveLicense(licenseKey, user);
        if (license == null) {
            return null; // Лицензия не найдена или принадлежит другому пользователю
        }

        if (!verifyDeviceLinking(device, license)) {
            return null; // Не удалось привязать устройство (лимит устройств или уже привязано)
        }


        linkDeviceToLicense(device, license);
        updateLicenseExpirationIfRequired(license);

        licenseHistoryService.recordLicenseChange(license, user, "Активирована", "Успешная активация лицензии");
        return new Ticket(license, device);
    }

    private License validateAndRetrieveLicense(String licenseKey, User user) {
        License license = licenseRepository.findByKey(licenseKey).orElse(null);
        if (license == null) {
            return null; // Лицензия не найдена
        }

        if (license.getUser() == null) {
            license.setUser(user);
        } else if (!license.getUser().getId().equals(user.getId())) {
            return null; // Лицензия принадлежит другому пользователю
        }

        if (license.getActivationDate()==null){
            license.setActivationDate(new Date());
        }

        return license;
    }

    private boolean verifyDeviceLinking(Device device, License license) {
        if (countActiveDevicesForLicense(license) >= license.getDeviceCount()) {
            return false; // Превышен лимит устройств
        }

        boolean alreadyLinked = deviceLicenseService
                .existsByLicenseIdAndDeviceId(license.getId(), device.getId());

        return !alreadyLinked; // Устройство уже связано
    }

    private DeviceLicense linkDeviceToLicense(Device device, License license) {
        DeviceLicense deviceLicense = new DeviceLicense();
        deviceLicense.setLicense(license);
        deviceLicense.setDevice(device);
        deviceLicense.setActivationDate(new Date());
        deviceLicenseRepository.save(deviceLicense);
        license.setActivationDate(new Date());
        license.setBlocked(false);
        licenseRepository.save(license);
        return deviceLicense;
    }

    private void updateLicenseExpirationIfRequired(License license) {
        if (license.getExpirationDate() == null) {
            int durationMonths = license.getLicenseType().getDefaultDuration();
            Date newExpirationDate = Date.from(Instant.now().plus(durationMonths, ChronoUnit.MONTHS));
            license.setExpirationDate(newExpirationDate);
        }
    }

    @Override
    public void deleteById(Long id) {
        licenseRepository.deleteById(id);
    }

    @Override
    public License getByKey(String key) {
        return licenseRepository.findByKey(key).orElse(null);
    }

    public Ticket renewLicense(String licenseKey, String macAddress, User authenticatedUser) {
        boolean isAdmin = authenticatedUser.getRole() == ApplicationRole.ADMIN;

        License license = getByKey(licenseKey);
        if (license == null) {
            return null; // Лицензия не найдена
        }
        User licenseOwner = license.getUser();
        if (!isAdmin && (licenseOwner == null || !licenseOwner.getEmail().equals(authenticatedUser.getEmail()))) {
            return null; // Нельзя продлить чужую лицензию
        }

        Device device = deviceRepository.findByMac(macAddress).orElse(null);
        if (device == null) {
            return null; // Устройство не найдено
        }

        boolean isLinked = deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId());
        if (!isLinked) {
            return null; // Устройство не связано с данной лицензией
        }

        Integer durationMonths = license.getLicenseType().getDefaultDuration();
        Date newExpirationDate = Date.from((license.getExpirationDate() != null ?
                license.getExpirationDate().toInstant() : Instant.now()).plus(durationMonths, ChronoUnit.MONTHS));
        license.setExpirationDate(newExpirationDate);
        licenseRepository.save(license);
        licenseHistoryService.recordLicenseChange(
                license, licenseOwner != null ? licenseOwner : authenticatedUser, "Продление", "Новая дата: " + newExpirationDate
        );
        return new Ticket(license, device);
    }

    @Override
    public boolean existsByLicenseTypeId(Long licenseTypeId) {
        return licenseRepository.existsByLicenseTypeId(licenseTypeId);
    }

    public long countActiveDevicesForLicense(License license) {
        return deviceLicenseRepository.countByLicenseAndActivationDateIsNotNull(license);
    }


    @GetMapping("/info")
    public ResponseEntity<?> getLicenseInfo(@RequestParam String mac, @RequestParam String licenseKey) {
        try {

            Device device = deviceService.getByMac(mac);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Устройство не найдено");
            }

            License license = licenseRepository.findByKey(licenseKey).orElse(null);
            if (license == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лицензия не найдена");
            }


            DeviceLicense deviceLicense = deviceLicenseRepository.findByDeviceIdAndLicenseId(device.getId(), license.getId());
            if (deviceLicense == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Связь между устройством и лицензией не найдена");
            }


            if (license.getExpirationDate() != null && license.getExpirationDate().before(new Date())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Лицензия истекла");
            }

            Ticket ticket = new Ticket(license, device);

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
        }
    }

    public void changeLicenseStatus(Long licenseId, boolean isBlocked, User authenticatedUser) {
        License license = licenseRepository.findById(licenseId).orElse(null);
        if (license == null) {
            return;
        }

        boolean previousStatus = license.getBlocked();

        if (previousStatus != isBlocked) {
            license.setBlocked(isBlocked);
            licenseRepository.save(license);

            String action = isBlocked ? "Блокировка" : "Разблокировка";
            String description = "Лицензия была " + (isBlocked ? "заблокирована" : "разблокирована");

            licenseHistoryService.recordLicenseChange(
                    license,
                    authenticatedUser,
                    action,
                    description
            );
        }
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return licenseRepository.existsByProductId(productId);
    }

    @Override
    public License findById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }
}
