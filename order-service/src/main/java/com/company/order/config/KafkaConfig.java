package com.company.order.config;

import com.company.order.dto.PaymentEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, PaymentEvent> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-payment-group"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        JsonDeserializer<PaymentEvent> deserializer =
                new JsonDeserializer<>(PaymentEvent.class);

        deserializer.addTrustedPackages("com.company.order.dto");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent>
            kafkaListenerContainerFactory(
                    ConsumerFactory<String, PaymentEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}