package ru.mtuci.rbpo_2024_praktika.service;

import ru.mtuci.rbpo_2024_praktika.model.Product;

import java.util.Optional;

public interface ProductService {
    Product getProductById(Long id);
    Product addProduct(Product product);
    Optional<Product> findById(Long id);
    void deleteById(Long id);
}
