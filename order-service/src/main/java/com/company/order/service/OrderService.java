package com.company.order.service;

import com.company.order.dto.OrderEvent;
import com.company.order.dto.OrderRequest;
import com.company.order.dto.OrderResponse;
import com.company.order.entity.Order;
import com.company.order.repository.OrderRepository;
import com.company.order.OrderServiceClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderResponse mapToResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .total(order.getTotal())
                .build();
    }

    public OrderResponse createOrder(OrderRequest request) {

        // Step 0: Check for duplicate order using idempotency key
        Order existingOrder =
                orderRepository
                        .findByIdempotencyKey(request.getIdempotencyKey())
                        .orElse(null);

        if (existingOrder != null) {

            System.out.println(
                    "Duplicate order request ignored for idempotencyKey -> "
                            + request.getIdempotencyKey()
            );

            return mapToResponse(existingOrder);
        }

        // Step 1: Validate user
        orderServiceClient.validateUser(
                request.getUserId()
        );

        // Step 2: Reserve inventory
        orderServiceClient.reserveInventory(
                request.getSkuCode(),
                request.getQuantity()
        );

        // Step 3: Create order
        Order order = Order.builder()
                .userId(request.getUserId())
                .total(request.getTotal())
                .status("CREATED")
                .skuCode(request.getSkuCode())
                .quantity(request.getQuantity())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Order savedOrder =
                orderRepository.save(order);

        // Step 4: Publish OrderEvent to Kafka
        OrderEvent event = OrderEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .total(savedOrder.getTotal())
                .status(savedOrder.getStatus())
                .build();

        kafkaTemplate.send(
                "order-events",
                event
        );

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {

        Order order =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found with id: "
                                                + id
                                )
                        );

        return mapToResponse(order);
    }
}