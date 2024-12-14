package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.model.LicenseType;
import ru.mtuci.rbpo_2024_praktika.model.Product;
import ru.mtuci.rbpo_2024_praktika.repository.ProductRepository;
import ru.mtuci.rbpo_2024_praktika.service.LicenseService;
import ru.mtuci.rbpo_2024_praktika.service.LicenseTypeService;
import ru.mtuci.rbpo_2024_praktika.service.ProductService;

import java.util.List;
import java.util.Optional;

//TODO: 1. Любой пользователь может получить полную информацию о любой лицензии? -

@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@RequestMapping("/info")
@RestController
public class InfoController {
    private final LicenseService licenseService;
    private final LicenseTypeService licenseTypeService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @GetMapping("/license")
    public ResponseEntity<?> getLicenseInfo(@RequestParam String mac, @RequestParam String key) {
        return licenseService.getLicenseInfo(mac,key);
    }

    @GetMapping("/license-type/all")
    public ResponseEntity<List<LicenseType>> getAllLicenseTypes() {
        try {
            List<LicenseType> licenseTypes = licenseTypeService.findAll();
            if (licenseTypes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(licenseTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/license-type/{id}")
    public ResponseEntity<LicenseType> getLicenseTypeById(@PathVariable Long id) {
        try {
            Optional<LicenseType> licenseType = licenseTypeService.findById(id);
            return licenseType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/product/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productService.findById(id);
            if (product.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(product.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
