package ru.mtuci.rbpo_2024_praktika.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.rbpo_2024_praktika.model.License;

import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByKey(String key);
    boolean existsByKey(String key);
    void delete(License license);
    boolean existsByProductId(Long productId);
    boolean existsByLicenseTypeId(Long licenseTypeId);
}
