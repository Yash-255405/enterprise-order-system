package com.company.payment.service;

import com.company.payment.dto.PaymentEvent;
import com.company.payment.dto.PaymentRequest;
import com.company.payment.dto.PaymentResponse;
import com.company.payment.entity.Payment;
import com.company.payment.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResponse processPayment(PaymentRequest request) {

        // Check if payment already exists for this order
        Payment existingPayment =
                paymentRepository.findByOrderId(request.getOrderId()).orElse(null);

        // If payment already exists, return the existing payment
        if (existingPayment != null) {

            // For testing Saga rollback:
            // amount = 0 changes the payment status to FAILED
            if (request.getAmount() != null && request.getAmount() == 0.0) {

                existingPayment.setStatus("FAILED");

                Payment updatedPayment =
                        paymentRepository.save(existingPayment);

                // Create FAILED payment event
                PaymentEvent paymentEvent = PaymentEvent.builder()
                        .paymentId(updatedPayment.getId())
                        .orderId(updatedPayment.getOrderId())
                        .status(updatedPayment.getStatus())
                        .build();

                // Send FAILED event to Kafka
                kafkaTemplate.send("payment-events", paymentEvent)
                        .whenComplete((result, ex) -> {

                            if (ex != null) {

                                System.out.println(
                                        "PAYMENT FAILED EVENT ERROR -> "
                                                + ex.getMessage()
                                );

                            } else {

                                System.out.println(
                                        "FAILED PAYMENT EVENT SENT -> "
                                                + result.getRecordMetadata()
                                );
                            }
                        });

                return PaymentResponse.builder()
                        .id(updatedPayment.getId())
                        .orderId(updatedPayment.getOrderId())
                        .status(updatedPayment.getStatus())
                        .build();
            }

            // Normal duplicate payment request
            System.out.println(
                    "Duplicate payment request ignored for orderId -> "
                            + request.getOrderId()
            );

            return PaymentResponse.builder()
                    .id(existingPayment.getId())
                    .orderId(existingPayment.getOrderId())
                    .status(existingPayment.getStatus())
                    .build();
        }

        // Create new payment
        String paymentStatus =
                request.getAmount() != null && request.getAmount() == 0.0
                        ? "FAILED"
                        : "SUCCESS";

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

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

                        System.out.println(
                                "PAYMENT EVENT FAILED -> "
                                        + ex.getMessage()
                        );

                    } else {

                        System.out.println(
                                "PAYMENT EVENT SENT -> "
                                        + result.getRecordMetadata()
                        );
                    }
                });

        return PaymentResponse.builder()
                .id(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .status(savedPayment.getStatus())
                .build();
    }
}