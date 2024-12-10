package ru.mtuci.rbpo_2024_praktika.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.rbpo_2024_praktika.model.License;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.ticket.Ticket;
public interface LicenseService {
    void add(License license);
    License getByKey(String key);
    License createLicense(Long productId, Long ownerId, Long licenseTypeId, Integer deviceCount);
    License findById(Long id);
    Ticket processActivation(String macAddress, String licenseKey, User user);
    Ticket renewLicense(String licenseKey, String macAddress, User authenticatedUser);
    ResponseEntity<?> getLicenseInfo(@RequestParam String mac);
    long countActiveDevicesForLicense(License license);
    void deleteById(Long id);
    void changeLicenseStatus(Long licenseId, boolean isBlocked, User authenticatedUser);
    boolean existsByProductId(Long id);
    boolean existsByLicenseTypeId(Long id);
}
