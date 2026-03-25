package com.iot.poc.iot_backend.controller;

import com.iot.poc.iot_backend.entity.DeviceTelemetry;
import com.iot.poc.iot_backend.entity.IotDevice;
import com.iot.poc.iot_backend.repository.IotDeviceRepository;
import com.iot.poc.iot_backend.service.DeviceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private IotDeviceRepository deviceRepository;

    @GetMapping
    public List<IotDevice> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/history")
    public List<DeviceTelemetry> getDeviceHistory(@RequestParam(required = false) String deviceId) {
        return deviceService.getHistory(deviceId);
    }
}