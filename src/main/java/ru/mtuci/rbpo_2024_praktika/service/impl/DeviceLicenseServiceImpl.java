package ru.mtuci.rbpo_2024_praktika.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo_2024_praktika.model.DeviceLicense;
import ru.mtuci.rbpo_2024_praktika.repository.DeviceLicenseRepository;
import ru.mtuci.rbpo_2024_praktika.service.DeviceLicenseService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceLicenseServiceImpl implements DeviceLicenseService {

    private final DeviceLicenseRepository deviceLicenseRepository;

    @Override
    public List<DeviceLicense> findByDeviceId(Long deviceId) {
        return deviceLicenseRepository.findByDeviceId(deviceId);
    }

    @Override
    public boolean existsByLicenseIdAndDeviceId(Long licenseId, Long deviceId){
        return deviceLicenseRepository.existsByLicenseIdAndDeviceId(licenseId, deviceId);
    }


}
