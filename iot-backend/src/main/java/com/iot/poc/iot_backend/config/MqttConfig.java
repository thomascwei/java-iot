package com.iot.poc.iot_backend.config;

import com.iot.poc.iot_backend.repository.IotDeviceRepository;
import com.iot.poc.iot_backend.service.DeviceService;

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

    @Autowired
    private DeviceService deviceService; // 注入剛剛建好的 Service

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

            deviceService.handleMqttMessage(topic, payload);
        };
    }
}