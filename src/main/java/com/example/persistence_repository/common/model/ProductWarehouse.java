package com.example.persistence_repository.common.model;

import com.example.persistence_repository.common.model.enums.ProductStatus;
import com.example.persistence_repository.common.model.enums.converter.ProductStatusConverter;
import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.convert.Convert;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "ProductWarehouse")
public class ProductWarehouse {
    @Key
    @Column(name = "ProductWarehouseID", type = "BIGINT")
    private Long productWarehouseID;

    @Column(name = "ProductStatus", length = 20)
    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus productStatus;

    @Column(name = "WarehouseID", type = "BIGINT", nullable = false)
    private Long warehouseID;

    @Column(name = "ItemID", type = "BIGINT", nullable = false)
    private Long itemID;

    @ManyToOne(joinColumn = "WarehouseID", fetch = FetchMode.EAGER)
    private LazyReference<Warehouse> warehouse;

    @ManyToOne(joinColumn = "ItemID", fetch = FetchMode.EAGER)
    private LazyReference<InventoryItem> inventoryItem;

    // Inverse side of one-to-one to ProductExported (optional / may be null until
    // exported)
    @OneToOne(mappedBy = "productWarehouse", joinColumn = "ProductWarehouseID", fetch = FetchMode.LAZY)
    private LazyReference<ProductExported> productExported;

    public Long getProductWarehouseID() {
        return productWarehouseID;
    }

    public void setProductWarehouseID(Long productWarehouseID) {
        this.productWarehouseID = productWarehouseID;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
    }

    public Long getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Long warehouseID) {
        this.warehouseID = warehouseID;
    }

    public Long getItemID() {
        return itemID;
    }

    public void setItemID(Long itemID) {
        this.itemID = itemID;
    }

    public Warehouse getWarehouse() {
        return warehouse.get();
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse.setValue(warehouse);
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem.get();
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem.setValue(inventoryItem);
    }

    public ProductExported getProductExported() {
        return productExported.get();
    }

    public void setProductExported(ProductExported productExported) {
        this.productExported.setValue(productExported);
    }
}
