package ru.mtuci.rbpo_2024_praktika.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.rbpo_2024_praktika.model.Device;
import ru.mtuci.rbpo_2024_praktika.model.User;
import ru.mtuci.rbpo_2024_praktika.repository.DeviceRepository;
import ru.mtuci.rbpo_2024_praktika.service.DeviceService;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public Device addDevice(String name, User user) {
        String mac = getMacAddress();

        if (deviceRepository.findByMac(mac).isPresent()) {
            throw new IllegalArgumentException("Устройство уже существует");
        }

        Device device = buildDevice(name, mac, user);

        return deviceRepository.save(device);
    }

    private Device buildDevice(String name, String mac, User user) {
        Device device = new Device();
        device.setName(name);
        device.setMac(mac);
        device.setUser(user);
        return device;
    }


    public static String getMacAddress() {
        try {
            for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (netInterface.isUp() && !netInterface.isLoopback() && netInterface.getHardwareAddress() != null) {
                    byte[] mac = netInterface.getHardwareAddress();
                    String macAddress = formatMacAddress(mac);
                    if (!macAddress.isEmpty()) {
                        return macAddress;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Ошибка при доступе к сетевым интерфейсам: " + e.getMessage());
        }
        return "MAC-адрес недоступен";
    }

    private static String formatMacAddress(byte[] mac) {
        if (mac == null) {
            return "";
        }
        StringBuilder macBuilder = new StringBuilder();
        for (byte b : mac) {
            if (macBuilder.length() > 0) {
                macBuilder.append('-');
            }
            macBuilder.append(String.format("%02X", b));
        }
        return macBuilder.toString();
    }

    public Device getByMac(String mac) {
        return deviceRepository.findByMac(mac)
                .orElseThrow(() -> new EntityNotFoundException("Устройство с MAC-адресом " + mac + " не найдено"));
    }
}