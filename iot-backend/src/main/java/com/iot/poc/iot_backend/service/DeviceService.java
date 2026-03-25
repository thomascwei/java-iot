package com.iot.poc.iot_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.poc.iot_backend.entity.DeviceTelemetry;
import com.iot.poc.iot_backend.repository.DeviceTelemetryRepository;
import com.iot.poc.iot_backend.repository.IotDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private IotDeviceRepository deviceRepository;

    @Autowired
    private DeviceTelemetryRepository telemetryRepository; // SQL 歷史紀錄

    @Autowired
    private StringRedisTemplate redisTemplate; // Redis Hot Data

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

            // === 1. Hot Data (Redis) ===
            // 存儲最新狀態與數值，並設定過期時間（例如 10 分鐘沒更新視為斷線）
            String redisKey = "device:status:" + deviceId;
            System.out.println("DEBUG: 準備寫入 Redis -> Key: " + redisKey + ", Value: " + payload);

            try {
                redisTemplate.opsForValue().set(redisKey, payload, Duration.ofMinutes(10));
                System.out.println("SUCCESS: Redis 寫入成功！");
            } catch (Exception e) {
                System.err.println("ERROR: Redis 寫入失敗！錯誤原因: " + e.getMessage());
            }
            // === 2. Cold Data (SQL: 歷史紀錄) ===
            DeviceTelemetry history = new DeviceTelemetry();
            history.setDeviceId(deviceId);
            if (jsonNode.has("temp"))
                history.setTemperature(jsonNode.get("temp").asDouble());
            if (jsonNode.has("humidity"))
                history.setHumidity(jsonNode.get("humidity").asDouble());
            telemetryRepository.save(history);

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

    public List<DeviceTelemetry> getHistory(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return telemetryRepository.findAllByOrderByCreatedAtDesc();
        }
        return telemetryRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }
}