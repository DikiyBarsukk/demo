package ru.mtuci.rbpo_2024_praktika.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo_2024_praktika.model.LicenseType;
import ru.mtuci.rbpo_2024_praktika.repository.LicenseTypeRepository;
import ru.mtuci.rbpo_2024_praktika.service.LicenseTypeService;

import java.util.List;
import java.util.Optional;

@Service
public class LicenseTypeServiceImpl implements LicenseTypeService {

    @Autowired
    private LicenseTypeRepository licenseTypeRepository;

    @Override
    public void deleteById(Long id) {
        licenseTypeRepository.deleteById(id);
    }

    @Override
    public LicenseType getLicenseTypeById(Long id) {
        return licenseTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Тип лицензии не найден"));
    }

    @Override
    public LicenseType addLicenseType(LicenseType licenseType) {
        if (licenseTypeRepository.existsByName(licenseType.getName())) {
            throw new IllegalArgumentException("Тип лицензии уже существует");
        }

        return licenseTypeRepository.save(licenseType);
    }

    @Override
    public List<LicenseType> findAll(){
        return licenseTypeRepository.findAll();
    }

    @Override
    public Optional<LicenseType> findById(Long id){
        return licenseTypeRepository.findById(id);
    }

}
