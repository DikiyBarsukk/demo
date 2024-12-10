package ru.mtuci.rbpo_2024_praktika.service;

import ru.mtuci.rbpo_2024_praktika.model.DeviceLicense;
import java.util.List;

public interface DeviceLicenseService {
    boolean existsByLicenseIdAndDeviceId(Long licenseId, Long deviceId);
    List<DeviceLicense> findByDeviceId(Long deviceId);
}