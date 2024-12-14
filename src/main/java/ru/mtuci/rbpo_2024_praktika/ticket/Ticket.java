package ru.mtuci.rbpo_2024_praktika.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mtuci.rbpo_2024_praktika.model.Device;
import ru.mtuci.rbpo_2024_praktika.model.License;
import ru.mtuci.rbpo_2024_praktika.model.TicketUtility;

import java.util.Date;

//TODO: 1. Интересная реализация, хочется с вами обсудить подробнее

@NoArgsConstructor
@Data
public class Ticket {

    private Date serverDate;
    private Long ticketLifetime;
    private Date activationDate;
    private Date expirationDate;
    private Long userId;
    private String deviceId;
    private String licenseBlocked;
    private String digitalSignature;
    @JsonIgnore
    private License license;
    @JsonIgnore
    private Device device;

    @Builder
    public Ticket(License license, Device device) {
        this.serverDate = new Date();
        this.ticketLifetime = license.getLicenseType().getDefaultDuration().longValue() * 30 * 24 * 60 * 60;
        this.activationDate = license.getActivationDate();
        this.expirationDate = license.getExpirationDate();
        this.userId = device.getUser() != null ? device.getUser().getId() : null;
        this.deviceId = device.getMac();
        this.licenseBlocked = license.getBlocked() != null ? license.getBlocked().toString() : "null";
        this.digitalSignature= TicketUtility.getInstance().generateDigitalSignature();
    }
}


