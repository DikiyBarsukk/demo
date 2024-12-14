package ru.mtuci.rbpo_2024_praktika.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.rbpo_2024_praktika.model.*;
import ru.mtuci.rbpo_2024_praktika.repository.ProductRepository;
import ru.mtuci.rbpo_2024_praktika.service.LicenseService;
import ru.mtuci.rbpo_2024_praktika.service.LicenseTypeService;
import ru.mtuci.rbpo_2024_praktika.service.ProductService;
import ru.mtuci.rbpo_2024_praktika.service.UserService;
import ru.mtuci.rbpo_2024_praktika.ticket.Ticket;
import ru.mtuci.rbpo_2024_praktika.util.AuthUtil;

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
    private final UserService userService;
    private final AuthUtil authUtil;

    /**
     * Теперь метод требует userId, чтобы уточнить,
     * смотрим ли мы лицензию того же пользователя, который делает запрос,
     * или администратор просматривает чужие лицензии.
     */
    @GetMapping("/license")
    public ResponseEntity<?> getLicenseInfo(
            @RequestParam String mac,
            @RequestParam String key,
            @RequestParam Long userId  // добавил параметр владельца
    ) {
        try {

            User currentUser = authUtil.getAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            }


            ResponseEntity<?> response = licenseService.getLicenseInfo(mac, key);
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            Ticket ticket = (Ticket) response.getBody();
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лицензия не найдена");
            }

            License license = ticket.getLicense();
            if (license == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка: некорректная лицензия");
            }


            boolean isOwner = (license.getUser() != null && license.getUser().getId().equals(userId));
            boolean isCurrentUserOwner = (license.getUser() != null && license.getUser().getId().equals(currentUser.getId()));
            boolean isAdmin =  currentUser.getRole() == ApplicationRole.ADMIN;


            if (!isAdmin && !isCurrentUserOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас нет прав на просмотр чужой лицензии.");
            }


            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Параметр userId не совпадает с владельцем лицензии");
            }


            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Получение списка LicenseType
     */
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

    /**
     * Получение LicenseType по ID
     */
    @GetMapping("/license-type/{id}")
    public ResponseEntity<LicenseType> getLicenseTypeById(@PathVariable Long id) {
        try {
            Optional<LicenseType> licenseType = licenseTypeService.findById(id);
            return licenseType.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Получение списка всех продуктов
     */
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

    /**
     * Получение продукта по ID
     */
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
