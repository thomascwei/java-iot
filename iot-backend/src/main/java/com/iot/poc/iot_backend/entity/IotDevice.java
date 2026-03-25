package com.iot.poc.iot_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "iot_devices")
@EntityListeners(AuditingEntityListener.class) // 讓 JPA 監聽實體變化
public class IotDevice {

    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    @CreatedDate // 新增時自動填入
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    @LastModifiedDate // 每次 save() 時自動更新
    private OffsetDateTime updatedAt;
}