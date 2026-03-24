package com.iot.poc.iot_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iot.poc.iot_backend.entity.IotDevice;

@Repository
public interface IotDeviceRepository extends JpaRepository<IotDevice, String> {
    // 基本的 findById, save, delete 已經內建了
}