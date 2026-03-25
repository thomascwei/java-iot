package com.iot.poc.iot_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "device_telemetry")
@EntityListeners(AuditingEntityListener.class)
public class DeviceTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private Double temperature;
    private Double humidity;

    @CreatedDate
    @Column(updatable = false)
    private OffsetDateTime createdAt;
}