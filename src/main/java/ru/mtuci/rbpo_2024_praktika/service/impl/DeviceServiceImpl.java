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
import java.util.Random;

//+TODO: 1. Я вижу уже раз третий этот непонятный способ получения мак адреса. Чей вы mac-адрес берёте?
// MAC не берётся с сервера, а генерируется локально. Можно использовать этот подход,
// если не нужно фиксировать реальный MAC-адрес.

/*
    // Пример генерации случайного MAC-адреса
    private static String generateRandomMac() {
        byte[] mac = new byte[6];
        new Random().nextBytes(mac);

        // Устанавливаем локально-администрируемый бит (0x02) и сбрасываем мультикаст-бит (0x01).
        mac[0] = (byte)(mac[0] & 0xFE); // Сбрасываем младший бит (мультикаст)
        mac[0] = (byte)(mac[0] | 0x02); // Устанавливаем локально-администрируемый бит

        return formatMacAddress(mac);
    }
*/

@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Device addDevice(String name, User user) {
        // можно заменить getMacAddress() на generateRandomMac()
        String mac = getMacAddress();

        if (mac.isEmpty()) {
            throw new IllegalStateException("Не удалось получить MAC-адрес");
        }

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
                    String macAddress = formatMacAddress(netInterface.getHardwareAddress());
                    if (!macAddress.isEmpty()) {
                        return macAddress;
                    }
                }
            }
        } catch (SocketException e) {
        }
        return "";
    }

    private static String formatMacAddress(byte[] mac) {
        if (mac == null || mac.length == 0) {
            return "";
        }
        StringBuilder macBuilder = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            macBuilder.append(String.format("%02X", mac[i]));
            if (i < mac.length - 1) {
                macBuilder.append('-');
            }
        }
        return macBuilder.toString();
    }

    @Override
    public Device getByMac(String mac) {
        return deviceRepository.findByMac(mac)
                .orElseThrow(() -> new EntityNotFoundException("Устройство с MAC-адресом " + mac + " не найдено"));
    }
}
