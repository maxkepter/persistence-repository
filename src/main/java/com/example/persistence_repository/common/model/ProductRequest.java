package com.example.persistence_repository.common.model;

import com.example.persistence_repository.common.model.enums.ProductRequestStatus;
import com.example.persistence_repository.common.model.enums.converter.ProductRequestStatusConverter;
import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.convert.Convert;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.sql.Date;

@Entity(tableName = "ProductRequest")
public class ProductRequest {
    @Key
    @Column(name = "ProductRequestID", type = "BIGINT")
    private Long productRequestID;

    @Column(name = "Quantity", type = "INT", nullable = false)
    private Integer quantity;

    @Column(name = "RequestDate", type = "DATE", nullable = false)
    private Date requestDate;

    @Column(name = "Status", length = 20)
    @Convert(converter = ProductRequestStatusConverter.class)
    private ProductRequestStatus status;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "RequestID", type = "BIGINT", nullable = false)
    private Long requestID;

    @Column(name = "ProductID", type = "BIGINT", nullable = false)
    private Long productID;

    @Column(name = "WarehouseID", type = "BIGINT", nullable = false)
    private Long warehouseID;

    @ManyToOne(joinColumn = "RequestID", fetch = FetchMode.EAGER)
    private LazyReference<Request> request;

    @ManyToOne(joinColumn = "ProductID", fetch = FetchMode.EAGER)
    private LazyReference<Product> product;

    @ManyToOne(joinColumn = "WarehouseID", fetch = FetchMode.EAGER)
    private LazyReference<Warehouse> warehouse;

    public Long getProductRequestID() {
        return productRequestID;
    }

    public void setProductRequestID(Long productRequestID) {
        this.productRequestID = productRequestID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public ProductRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ProductRequestStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRequestID() {
        return requestID;
    }

    public void setRequestID(Long requestID) {
        this.requestID = requestID;
    }

    public Long getProductID() {
        return productID;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }

    public Long getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Long warehouseID) {
        this.warehouseID = warehouseID;
    }

    public Request getRequest() {
        return request.get();
    }

    public void setRequest(Request request) {
        this.request.setValue(request);
    }

    public Product getProduct() {
        return product.get();
    }

    public void setProduct(Product product) {
        this.product.setValue(product);
    }

    public Warehouse getWarehouse() {
        return warehouse.get();
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse.setValue(warehouse);
    }
}
