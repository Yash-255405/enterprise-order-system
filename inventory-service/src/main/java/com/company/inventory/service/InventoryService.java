package com.company.inventory.service;

import com.company.inventory.dto.InventoryResponse;
import com.company.inventory.entity.Inventory;
import com.company.inventory.repository.InventoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(inv -> InventoryResponse.builder()
                        .id(inv.getId())
                        .skuCode(inv.getSkuCode())
                        .quantity(inv.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    // Reserve inventory for an order
    public boolean reserveInventory(String skuCode, Integer quantity) {

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElse(null);

        // SKU does not exist
        if (inventory == null) {
            return false;
        }

        // Not enough stock
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient inventory for SKU: " + skuCode
                            + ". Requested: " + quantity
                            + ", Available: " + inventory.getQuantity()
            );
        }

        // Reserve the requested quantity
        inventory.setQuantity(
                inventory.getQuantity() - quantity
        );

        inventoryRepository.save(inventory);

        return true;
    }

    // Release inventory during Saga rollback
    public boolean releaseInventory(String skuCode, Integer quantity) {

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElse(null);

        // SKU does not exist
        if (inventory == null) {
            return false;
        }

        // Add the quantity back
        inventory.setQuantity(
                inventory.getQuantity() + quantity
        );

        inventoryRepository.save(inventory);

        return true;
    }
}