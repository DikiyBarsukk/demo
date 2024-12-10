package ru.mtuci.rbpo_2024_praktika.service;

import ru.mtuci.rbpo_2024_praktika.model.License;
import ru.mtuci.rbpo_2024_praktika.model.User;

public interface LicenseHistoryService {
    void recordLicenseChange(License license, User user, String status, String description);
}
