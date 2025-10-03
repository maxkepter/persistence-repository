package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.sql.Date;

@Entity(tableName = "WarehouseLog")
public class WarehouseLog {
    @Key
    @Column(name = "WarehouseLogID", type = "BIGINT")
    private Long warehouseLogID;

    @Column(name = "LogDate", type = "DATE", nullable = false)
    private Date logDate;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "WarehouseID", type = "BIGINT", nullable = false)
    private Long warehouseID;

    @Column(name = "ProductRequestID", type = "BIGINT", nullable = false)
    private Long productRequestID;

    @ManyToOne(joinColumn = "WarehouseID", fetch = FetchMode.EAGER)
    private LazyReference<Warehouse> warehouse;

    @ManyToOne(joinColumn = "ProductRequestID", fetch = FetchMode.EAGER)
    private LazyReference<ProductRequest> productRequest;

    public Long getWarehouseLogID() {
        return warehouseLogID;
    }

    public void setWarehouseLogID(Long warehouseLogID) {
        this.warehouseLogID = warehouseLogID;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Long warehouseID) {
        this.warehouseID = warehouseID;
    }

    public Long getProductRequestID() {
        return productRequestID;
    }

    public void setProductRequestID(Long productRequestID) {
        this.productRequestID = productRequestID;
    }

    public Warehouse getWarehouse() {
        return warehouse.get();
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse.setValue(warehouse);
    }

    public ProductRequest getProductRequest() {
        return productRequest.get();
    }

    public void setProductRequest(ProductRequest productRequest) {
        this.productRequest.setValue(productRequest);
    }
}
