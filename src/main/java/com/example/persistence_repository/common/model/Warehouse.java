package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "Warehouse")
public class Warehouse {
    @Key
    @Column(name = "WarehouseID", type = "BIGINT")
    private Long warehouseID;

    @Column(name = "WarehouseName", length = 100, nullable = false)
    private String warehouseName;

    @Column(name = "Location", length = 255)
    private String location;

    @Column(name = "WarehouseManager", length = 255, nullable = false)
    private String warehouseManager;

    @ManyToOne(joinColumn = "WarehouseManager", fetch = FetchMode.EAGER)
    private LazyReference<Account> managerAccount;

    public Long getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Long warehouseID) {
        this.warehouseID = warehouseID;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWarehouseManager() {
        return warehouseManager;
    }

    public void setWarehouseManager(String warehouseManager) {
        this.warehouseManager = warehouseManager;
    }

    public Account getManagerAccount() {
        return managerAccount.get();
    }

    public void setManagerAccount(Account managerAccount) {
        this.managerAccount.setValue(managerAccount);
    }
}
