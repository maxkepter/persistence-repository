package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "ProductContract")
public class ProductContract {
    @Key
    @Column(name = "ContractID", type = "BIGINT", nullable = false)
    private Long contractID;

    @Column(name = "ItemID", type = "BIGINT", nullable = false)
    private Long itemID;

    @ManyToOne(joinColumn = "ContractID", fetch = FetchMode.EAGER)
    private LazyReference<Contract> contract;

    @ManyToOne(joinColumn = "ItemID", fetch = FetchMode.EAGER)
    private LazyReference<InventoryItem> inventoryItem;

    public Long getContractID() {
        return contractID;
    }

    public void setContractID(Long contractID) {
        this.contractID = contractID;
    }

    public Long getItemID() {
        return itemID;
    }

    public void setItemID(Long itemID) {
        this.itemID = itemID;
    }

    public Contract getContract() {
        return contract.get();
    }

    public void setContract(Contract contract) {
        this.contract.setValue(contract);
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem.get();
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem.setValue(inventoryItem);
    }
}
