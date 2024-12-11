package ru.mtuci.rbpo_2024_praktika.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.rbpo_2024_praktika.model.DeviceLicense;
import ru.mtuci.rbpo_2024_praktika.model.License;

import java.util.List;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    List<DeviceLicense> findByDeviceId(Long deviceId);
    long countByLicenseAndActivationDateIsNotNull(License license);
    boolean existsByLicenseIdAndDeviceId(Long licenseId, Long deviceId);
    DeviceLicense findByDeviceIdAndLicenseId(Long deviceId, Long licenseId);


}
