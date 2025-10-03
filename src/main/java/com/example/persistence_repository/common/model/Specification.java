package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.util.List;

@Entity(tableName = "Specification")
public class Specification {
    @Key
    @Column(name = "SpecificationID", type = "BIGINT")
    private Long specificationID;

    @Column(name = "SpecificationName", length = 100, nullable = false)
    private String specificationName;

    @Column(name = "SpecificationValue", length = 255, nullable = false)
    private String specificationValue;

    @Column(name = "SpecificationTypeID", type = "BIGINT", nullable = false)
    private Long specificationTypeID;

    @ManyToOne(joinColumn = "SpecificationTypeID", fetch = FetchMode.EAGER)
    private LazyReference<SpecificationType> specificationType;

    @OneToMany(mappedBy = "specificationID", joinColumn = "SpecificationID", fetch = FetchMode.LAZY)
    private List<ProductSpecification> productSpecifications;

    public Long getSpecificationID() {
        return specificationID;
    }

    public void setSpecificationID(Long specificationID) {
        this.specificationID = specificationID;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }

    public String getSpecificationValue() {
        return specificationValue;
    }

    public void setSpecificationValue(String specificationValue) {
        this.specificationValue = specificationValue;
    }

    public Long getSpecificationTypeID() {
        return specificationTypeID;
    }

    public void setSpecificationTypeID(Long specificationTypeID) {
        this.specificationTypeID = specificationTypeID;
    }

    public SpecificationType getSpecificationType() {
        return specificationType.get();
    }

    public void setSpecificationType(SpecificationType specificationType) {
        this.specificationType.setValue(specificationType);
    }

    public List<ProductSpecification> getProductSpecifications() {
        return productSpecifications;
    }

    public void setProductSpecifications(List<ProductSpecification> productSpecifications) {
        this.productSpecifications = productSpecifications;
    }
}
