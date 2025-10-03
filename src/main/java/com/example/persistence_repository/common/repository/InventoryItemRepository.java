package com.example.persistence_repository.common.repository;

import com.example.persistence_repository.common.model.InventoryItem;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class InventoryItemRepository extends AbstractRepository<InventoryItem, Long> {
    public InventoryItemRepository() {
        super(InventoryItem.class);
    }
}
