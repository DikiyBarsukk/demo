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
import java.util.*;
import java.util.stream.Stream;

//TODO: 1. Плохой способ борьбы с коллизиями кодов активации
//TODO: 2. По вашей логике, пользователь не может активировать лицензию с нового устройства
//TODO: 3. Где лицензии проставляется дата первой активации?
//TODO: 4. Возвращать список тикетов не нужно, вернуть только один тикет

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
            throw new IllegalArgumentException("Продукт не найден");
        }

        User user = userService.getById(ownerId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        LicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId);
        if (licenseType == null) {
            throw new NoSuchElementException("Тип лицензии не найден");
        }

        License license = new License();
        license.setProduct(product);
        license.setOwner(user);
        license.setLicenseType(licenseType);
        license.setDeviceCount(deviceCount);

        String activationCode = Stream.generate(() -> UUID.randomUUID().toString())
                .filter(code -> !licenseRepository.existsByKey(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Не удалось сгенерировать уникальный ключ активации"));
        license.setKey(activationCode);
        licenseRepository.save(license);
        licenseHistoryService.recordLicenseChange(license, user, "Создана", "Лицензия успешно создана");
        return license;
    }

    @Override
    public Ticket processActivation(String macAddress, String licenseKey, User user) {
        Device device = deviceRepository.findByMac(macAddress)
                .orElseThrow(() -> new IllegalArgumentException("Устройство с указанным MAC-адресом не найдено."));

        License license = validateAndRetrieveLicense(licenseKey, user);

        verifyDeviceLinking(device, license);

        linkDeviceToLicense(device, license);
        updateLicenseExpirationIfRequired(license);

        licenseHistoryService.recordLicenseChange(license, user, "Активирована", "Успешная активация лицензии");
        return new Ticket(license, device);
    }

    private License validateAndRetrieveLicense(String licenseKey, User user) {
        License license = licenseRepository.findByKey(licenseKey)
                .orElseThrow(() -> new IllegalArgumentException("Лицензия с указанным ключом не найдена."));

        if (license.getUser() == null) {
            license.setUser(user);
        }

        if (!license.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Лицензия зарегистрирована на другого пользователя.");
        }

        return license;
    }

    private void verifyDeviceLinking(Device device, License license) {
        if (countActiveDevicesForLicense(license) >= license.getDeviceCount()) {
            throw new IllegalArgumentException("Превышено допустимое количество устройств для данной лицензии.");
        }

        boolean alreadyLinked = deviceLicenseService
                .existsByLicenseIdAndDeviceId(license.getId(), device.getId());

        if (alreadyLinked) {
            throw new IllegalArgumentException("Устройство уже связано с данной лицензией.");
        }
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
            int duration = license.getLicenseType().getDefaultDuration();
            LocalDate expiration = LocalDate.now().plusMonths(duration);
            license.setExpirationDate(Date.from(expiration.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            licenseRepository.save(license);
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
            throw new IllegalArgumentException("Лицензия не найдена по указанному ключу.");
        }
        User licenseOwner = license.getUser();
        if (!isAdmin && (licenseOwner == null || !licenseOwner.getEmail().equals(authenticatedUser.getEmail()))) {
            throw new IllegalArgumentException("Вы не можете продлить чужую лицензию.");
        }

        Device device = deviceRepository.findByMac(macAddress)
                .orElseThrow(() -> new IllegalArgumentException("Устройство с таким MAC-адресом не найдено."));

        boolean isLinked = deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId());
        if (!isLinked) {
            throw new IllegalArgumentException("Устройство не связано с данной лицензией.");
        }

        Integer durationMonths = license.getLicenseType().getDefaultDuration();
        LocalDate baseDate = (license.getExpirationDate() != null)
                ? Instant.ofEpochMilli(license.getExpirationDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                : LocalDate.now();
        LocalDate updatedDate = baseDate.plusMonths(durationMonths);
        Date newExpirationDate = Date.from(updatedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
    public ResponseEntity<?> getLicenseInfo(@RequestParam String mac) {
        try {
            Device device = deviceService.getByMac(mac);
            if (device == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Устройство не найдено");
            }

            List<DeviceLicense> deviceLicenses = deviceLicenseService.findByDeviceId(device.getId());

            List<Ticket> tickets = new ArrayList<>();
            deviceLicenses.stream()
                    .map(DeviceLicense::getLicense)
                    .filter(Objects::nonNull)
                    .filter(license -> license.getExpirationDate() == null ||
                            !license.getExpirationDate().before(new Date()))
                    .map(license -> new Ticket(license, device))
                    .forEach(tickets::add);


            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
        }
    }

    public void changeLicenseStatus(Long licenseId, boolean isBlocked, User authenticatedUser) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("Лицензия с таким ID не найдена"));

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
