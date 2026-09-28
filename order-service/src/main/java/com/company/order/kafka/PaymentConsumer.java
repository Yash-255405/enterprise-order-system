package com.company.order.kafka;

import com.company.order.dto.PaymentEvent;
import com.company.order.entity.Order;
import com.company.order.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVENTORY_RELEASE_URL =
            "http://localhost:8084/api/inventory/release";

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-payment-group"
    )
    public void consume(PaymentEvent event) {

        System.out.println(
                "Payment event received in order service -> " + event
        );

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: "
                                        + event.getOrderId()
                        )
                );

        // Payment successful
        if ("SUCCESS".equals(event.getStatus())) {

            order.setStatus("PAID");

            orderRepository.save(order);

            System.out.println(
                    "Order status updated to PAID -> "
                            + order.getId()
            );
        }

        // Payment failed -> Saga compensation
        else if ("FAILED".equals(event.getStatus())) {

            System.out.println(
                    "Payment failed. Starting inventory rollback for order -> "
                            + order.getId()
            );

            String inventoryUrl =
                    INVENTORY_RELEASE_URL
                            + "?skuCode=" + order.getSkuCode()
                            + "&quantity=" + order.getQuantity();

            restTemplate.postForObject(
                    inventoryUrl,
                    null,
                    String.class
            );

            order.setStatus("PAYMENT_FAILED");

            orderRepository.save(order);

            System.out.println(
                    "Inventory released and order marked PAYMENT_FAILED -> "
                            + order.getId()
            );
        }
    }
}