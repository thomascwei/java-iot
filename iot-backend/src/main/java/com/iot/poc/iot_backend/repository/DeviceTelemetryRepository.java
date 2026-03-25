package com.iot.poc.iot_backend.repository;

import com.iot.poc.iot_backend.entity.DeviceTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceTelemetryRepository extends JpaRepository<DeviceTelemetry, Long> {
    // 根據 deviceId 查詢，並按建立時間倒序排列（最新的在前）
    List<DeviceTelemetry> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    // 查詢所有資料，並按建立時間倒序排列
    List<DeviceTelemetry> findAllByOrderByCreatedAtDesc();
}