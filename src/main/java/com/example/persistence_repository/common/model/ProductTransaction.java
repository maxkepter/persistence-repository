package com.example.persistence_repository.common.model;

import com.example.persistence_repository.common.model.enums.TransactionStatus;
import com.example.persistence_repository.common.model.enums.converter.TransactionStatusConverter;
import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.convert.Convert;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.time.LocalDateTime;

@Entity(tableName = "ProductTransaction")
public class ProductTransaction {
    @Key
    @Column(name = "TransactionID", type = "BIGINT")
    private Long transactionID;

    @Column(name = "TransactionDate", type = "DATETIME", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "SourceWarehouse", type = "BIGINT")
    private Long sourceWarehouseID;

    @Column(name = "DestinationWarehouse", type = "BIGINT")
    private Long destinationWarehouseID;

    @Column(name = "TransactionStatus", length = 20, nullable = false)
    @Convert(converter = TransactionStatusConverter.class)
    private TransactionStatus transactionStatus;

    @Column(name = "ItemID", type = "BIGINT", nullable = false)
    private Long itemID;

    @Column(name = "Note", length = 255)
    private String note;

    @ManyToOne(joinColumn = "ItemID", fetch = FetchMode.EAGER)
    private LazyReference<InventoryItem> inventoryItem;

    @ManyToOne(joinColumn = "SourceWarehouse", fetch = FetchMode.EAGER)
    private LazyReference<Warehouse> sourceWarehouse;

    @ManyToOne(joinColumn = "DestinationWarehouse", fetch = FetchMode.EAGER)
    private LazyReference<Warehouse> destinationWarehouse;

    public Long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Long transactionID) {
        this.transactionID = transactionID;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getDestinationWarehouseID() {
        return destinationWarehouseID;
    }

    public void setDestinationWarehouseID(Long destinationWarehouseID) {
        this.destinationWarehouseID = destinationWarehouseID;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Long getItemID() {
        return itemID;
    }

    public void setItemID(Long itemID) {
        this.itemID = itemID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem.get();
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem.setValue(inventoryItem);
    }

    public Warehouse getSourceWarehouseEntity() {
        return sourceWarehouse.get();
    }

    public void setSourceWarehouseEntity(Warehouse sourceWarehouse) {
        this.sourceWarehouse.setValue(sourceWarehouse);
    }

    public Long getSourceWarehouseID() {
        return sourceWarehouseID;
    }

    public void setSourceWarehouseID(Long sourceWarehouseID) {
        this.sourceWarehouseID = sourceWarehouseID;
    }

    public Warehouse getDestinationWarehouseEntity() {
        return destinationWarehouse.get();
    }

    public void setDestinationWarehouseEntity(Warehouse destinationWarehouse) {
        this.destinationWarehouse.setValue(destinationWarehouse);
    }
}
