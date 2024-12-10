package ru.mtuci.rbpo_2024_praktika.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo_2024_praktika.model.License;
import ru.mtuci.rbpo_2024_praktika.model.LicenseHistory;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.repository.LicenseHistoryRepository;
import ru.mtuci.rbpo_2024_praktika.service.LicenseHistoryService;

import java.util.Date;


@Service
public class LicenseHistoryServiceImpl implements LicenseHistoryService {

    @Autowired
    private LicenseHistoryRepository licenseHistoryRepository;

    @Override
    public void recordLicenseChange(License license, User user, String status, String description) {
        LicenseHistory licenseHistory = new LicenseHistory();
        licenseHistory.setLicense(license);
        licenseHistory.setUser(user);
        licenseHistory.setStatus(status);
        licenseHistory.setDescription(description);
        licenseHistory.setChangeDate(new Date());

        licenseHistoryRepository.save(licenseHistory);
    }
}
