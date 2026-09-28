package com.company.order;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL =
            "http://localhost:8081/api/users/";

    private static final String INVENTORY_SERVICE_URL =
            "http://localhost:8084/api/inventory/reserve";


    // =========================
    // USER SERVICE
    // =========================

    @Retry(
            name = "userServiceRetry",
            fallbackMethod = "userServiceFallback"
    )
    @CircuitBreaker(
            name = "userServiceCB"
    )
    public void validateUser(Long userId) {

        try {

            restTemplate.getForObject(
                    USER_SERVICE_URL + userId,
                    Object.class
            );

        } catch (HttpClientErrorException.NotFound e) {

            throw new UserNotFoundException(
                    "User not found with id: " + userId
            );
        }
    }


    public void userServiceFallback(
            Long userId,
            Throwable t) {

        if (t instanceof UserNotFoundException) {

            throw (UserNotFoundException) t;
        }

        System.out.println(
                "USER SERVICE RETRY / CIRCUIT BREAKER FALLBACK -> "
                        + t.getMessage()
        );

        throw new RuntimeException(
                "User Service is currently unavailable. "
                        + "Please try again later."
        );
    }


    // =========================
    // INVENTORY SERVICE
    // =========================

    @Retry(
            name = "inventoryServiceRetry",
            fallbackMethod = "inventoryServiceFallback"
    )
    @CircuitBreaker(
            name = "inventoryServiceCB"
    )
    public void reserveInventory(
            String skuCode,
            Integer quantity) {

        System.out.println(
                "INVENTORY CALL ATTEMPT -> skuCode="
                        + skuCode
                        + ", quantity="
                        + quantity
        );

        String inventoryUrl =
                INVENTORY_SERVICE_URL
                        + "?skuCode=" + skuCode
                        + "&quantity=" + quantity;

        try {

            restTemplate.postForObject(
                    inventoryUrl,
                    null,
                    String.class
            );

        } catch (HttpClientErrorException.BadRequest e) {

            // Inventory business failure.
            // This is NOT an Inventory Service outage.
            throw new InventoryReservationException(
                    e.getResponseBodyAsString()
            );
        }
    }


    public void inventoryServiceFallback(
            String skuCode,
            Integer quantity,
            Throwable t) {

        if (t instanceof InventoryReservationException) {

            throw (InventoryReservationException) t;
        }

        System.out.println(
                "INVENTORY SERVICE RETRY / CIRCUIT BREAKER FALLBACK -> "
                        + t.getMessage()
        );

        throw new RuntimeException(
                "Inventory Service is currently unavailable. "
                        + "Order cannot be created."
        );
    }
}