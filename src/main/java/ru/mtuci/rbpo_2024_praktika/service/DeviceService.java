package ru.mtuci.rbpo_2024_praktika.service;

import ru.mtuci.rbpo_2024_praktika.model.Device;
import ru.mtuci.rbpo_2024_praktika.model.User;

import java.util.List;
import java.util.Optional;

public interface DeviceService {
    Device addDevice(String name, User user);
    Device getByMac(String mac);
    Optional<Device> findById(Long id);
    List<Device> findAllByUserId(Long userId);
    Device save(Device device);
    void deleteById(Long id);
}
