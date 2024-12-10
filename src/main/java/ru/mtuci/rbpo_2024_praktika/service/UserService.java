package ru.mtuci.rbpo_2024_praktika.service;


import ru.mtuci.rbpo_2024_praktika.model.User;

import java.util.Optional;


public interface UserService {
    User getById(Long id);
    Optional<User> findByEmail(String email);
    void create(String email, String name, String password) ;
}
