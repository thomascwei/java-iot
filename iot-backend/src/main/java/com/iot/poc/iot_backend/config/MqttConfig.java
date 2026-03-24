package com.iot.poc.iot_backend.config;

import com.iot.poc.iot_backend.repository.IotDeviceRepository;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableIntegration
public class MqttConfig {

    @Autowired
    private IotDeviceRepository deviceRepository;

    // 1. 定義訊息通道
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 2. 設定 MQTT 連線參數
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { "tcp://localhost:1883" }); // 確保是 localhost
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    // 3. 設定接收器 (Adapter)，監聽主題
    @Bean
    public MessageProducer inbound() {
        // "java-backend-client" 是給 Java 用的 ID，不要跟 MQTTX 重複
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("java-backend-client",
                mqttClientFactory(), "iot/sensors/#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 4. 處理收到的訊息
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = message.getPayload().toString();
            ObjectMapper mapper = new ObjectMapper(); // 用於解析 JSON

            System.out.println("DEBUG: 收到主題 [" + topic + "] 的訊息: " + payload);

            try {
                // 1. 取得 Topic 中的設備 ID
                String[] parts = topic.split("/");
                if (parts.length >= 3) {
                    String deviceId = parts[2];

                    // 2. 解析 Payload 內容
                    JsonNode jsonNode = mapper.readTree(payload);

                    // 3. 判斷狀態：預設為 ONLINE，如果 JSON 裡明確說要關閉則設為 OFFLINE
                    String targetStatus = "ONLINE";
                    if (jsonNode.has("status")) {
                        String statusValue = jsonNode.get("status").asText().toUpperCase();
                        if ("OFFLINE".equals(statusValue) || "CLOSED".equals(statusValue)
                                || "OFF".equals(statusValue)) {
                            targetStatus = "OFFLINE";
                        }
                    }

                    // 4. 更新資料庫
                    final String finalStatus = targetStatus; // Lambda 需要 final 變數
                    deviceRepository.findById(deviceId).ifPresent(device -> {
                        device.setStatus(finalStatus);
                        deviceRepository.save(device);
                        System.out.println("SUCCESS: 設備 " + deviceId + " 狀態已更新為 " + finalStatus);
                    });
                }
            } catch (Exception e) {
                System.err.println("ERROR: 解析訊息失敗 - " + e.getMessage());
            }
        };
    }
}