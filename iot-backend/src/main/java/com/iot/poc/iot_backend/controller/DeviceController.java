package com.iot.poc.iot_backend.controller;

import com.iot.poc.iot_backend.entity.IotDevice;
import com.iot.poc.iot_backend.repository.IotDeviceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private IotDeviceRepository deviceRepository;

    @GetMapping
    public List<IotDevice> getAllDevices() {
        // 這會直接去資料庫撈出剛才那兩筆資料
        return deviceRepository.findAll();
    }
}