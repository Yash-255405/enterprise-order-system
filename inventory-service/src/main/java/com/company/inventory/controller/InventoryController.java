package com.company.inventory.controller;

import com.company.inventory.dto.InventoryResponse;
import com.company.inventory.entity.Inventory;
import com.company.inventory.repository.InventoryRepository;
import com.company.inventory.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;


    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }


    // Add inventory stock
    @PostMapping
    public ResponseEntity<Inventory> addInventory(
            @RequestParam String skuCode,
            @RequestParam Integer quantity) {

        Inventory inventory = Inventory.builder()
                .skuCode(skuCode)
                .quantity(quantity)
                .build();

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return ResponseEntity.ok(savedInventory);
    }


    // Reserve inventory
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveInventory(
            @RequestParam String skuCode,
            @RequestParam Integer quantity) {

        try {

            boolean reserved =
                    inventoryService.reserveInventory(
                            skuCode,
                            quantity
                    );

            if (!reserved) {

                return ResponseEntity
                        .badRequest()
                        .body("Inventory reservation failed");
            }

            return ResponseEntity
                    .ok()
                    .body("Inventory reserved successfully");

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // Release inventory during Saga rollback
    @PostMapping("/release")
    public ResponseEntity<String> releaseInventory(
            @RequestParam String skuCode,
            @RequestParam Integer quantity) {

        boolean released =
                inventoryService.releaseInventory(
                        skuCode,
                        quantity
                );

        if (!released) {

            return ResponseEntity
                    .badRequest()
                    .body("Inventory release failed");
        }

        return ResponseEntity
                .ok()
                .body("Inventory released successfully");
    }
}