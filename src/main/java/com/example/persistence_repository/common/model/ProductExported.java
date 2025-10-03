package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "ProductExported")
public class ProductExported {
    @Key
    @Column(name = "ProductWarehouseID", type = "BIGINT", nullable = false)
    private Long productWarehouseID;

    @Column(name = "WarehouseLogID", type = "BIGINT", nullable = false)
    private Long warehouseLogID;

    @OneToOne(joinColumn = "ProductWarehouseID", fetch = FetchMode.EAGER, mappedBy = "productWarehouseID")
    private LazyReference<ProductWarehouse> productWarehouse;

    @ManyToOne(joinColumn = "WarehouseLogID", fetch = FetchMode.EAGER)
    private LazyReference<WarehouseLog> warehouseLog;

    public Long getProductWarehouseID() {
        return productWarehouseID;
    }

    public void setProductWarehouseID(Long productWarehouseID) {
        this.productWarehouseID = productWarehouseID;
    }

    public Long getWarehouseLogID() {
        return warehouseLogID;
    }

    public void setWarehouseLogID(Long warehouseLogID) {
        this.warehouseLogID = warehouseLogID;
    }

    public ProductWarehouse getProductWarehouse() {
        return productWarehouse.get();
    }

    public void setProductWarehouse(ProductWarehouse productWarehouse) {
        this.productWarehouse.setValue(productWarehouse);
    }

    public WarehouseLog getWarehouseLog() {
        return warehouseLog.get();
    }

    public void setWarehouseLog(WarehouseLog warehouseLog) {
        this.warehouseLog.setValue(warehouseLog);
    }
}
