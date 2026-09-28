package com.company.payment.kafka;

import com.company.payment.dto.OrderEvent;
import com.company.payment.dto.PaymentEvent;
import com.company.payment.entity.Payment;
import com.company.payment.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void consume(OrderEvent event) {

        LOGGER.info(
                "Order event received in payment service -> {}",
                event
        );

        // Check if payment already exists for this order
        Payment existingPayment =
                paymentRepository.findByOrderId(event.getOrderId()).orElse(null);

        if (existingPayment != null) {

            LOGGER.info(
                    "Duplicate order event ignored for orderId -> {}",
                    event.getOrderId()
            );

            return;
        }

        // Create payment
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .status("SUCCESS")
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        LOGGER.info(
                "Payment saved successfully for orderId -> {}",
                event.getOrderId()
        );

        // Create payment event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .status(savedPayment.getStatus())
                .build();

        // Send payment event to Kafka
        kafkaTemplate.send("payment-events", paymentEvent)
                .whenComplete((result, ex) -> {

                    if (ex != null) {

                        LOGGER.error(
                                "PAYMENT EVENT FAILED -> {}",
                                ex.getMessage()
                        );

                    } else {

                        LOGGER.info(
                                "PAYMENT EVENT SENT -> {}",
                                result.getRecordMetadata()
                        );
                    }
                });
    }
}