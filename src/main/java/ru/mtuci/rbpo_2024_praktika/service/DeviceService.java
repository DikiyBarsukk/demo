package ru.mtuci.rbpo_2024_praktika.service;

import ru.mtuci.rbpo_2024_praktika.model.Device;
import ru.mtuci.rbpo_2024_praktika.model.User;

public interface DeviceService {
    Device addDevice(String name, User user);
    Device getByMac(String mac);

}