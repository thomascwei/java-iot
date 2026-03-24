package com.iot.poc.iot_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.poc.iot_backend.repository.IotDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    @Autowired
    private IotDeviceRepository deviceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 處理 MQTT 訊息的核心邏輯
     */
    public void handleMqttMessage(String topic, String payload) {
        try {
            // 1. 從 Topic 提取 Device ID (例如: iot/sensors/SN-001)
            String[] parts = topic.split("/");
            if (parts.length < 3)
                return;
            String deviceId = parts[2];

            // 2. 解析 JSON Payload
            JsonNode jsonNode = objectMapper.readTree(payload);

            // 3. 判斷目標狀態
            String targetStatus = determineStatus(jsonNode);

            // 4. 更新資料庫
            deviceRepository.findById(deviceId).ifPresentOrElse(device -> {
                device.setStatus(targetStatus);
                deviceRepository.save(device);
                System.out.println("SUCCESS: 設備 " + deviceId + " 狀態更新為 " + targetStatus);
            }, () -> {
                System.out.println("WARN: 找不到設備 " + deviceId + "，略過更新");
            });

        } catch (Exception e) {
            System.err.println("ERROR: 解析 MQTT 訊息失敗: " + e.getMessage());
        }
    }

    private String determineStatus(JsonNode node) {
        if (node.has("status")) {
            String status = node.get("status").asText().toLowerCase();
            if (status.equals("offline") || status.equals("off") || status.equals("closed")) {
                return "OFFLINE";
            }
        }
        return "ONLINE";
    }
}