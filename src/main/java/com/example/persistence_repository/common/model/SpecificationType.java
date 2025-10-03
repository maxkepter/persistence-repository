package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.util.List;

@Entity(tableName = "SpecificationType")
public class SpecificationType {
    @Key
    @Column(name = "SpecificationTypeID", type = "BIGINT")
    private Long specificationTypeID;

    @Column(name = "SpecificationTypeName", length = 100, nullable = false)
    private String specificationTypeName;

    @Column(name = "TypeID", type = "BIGINT", nullable = false)
    private Long typeID;

    @ManyToOne(joinColumn = "TypeID", fetch = FetchMode.EAGER)
    private LazyReference<Type> type;

    @OneToMany(mappedBy = "specificationTypeID", joinColumn = "SpecificationTypeID", fetch = FetchMode.LAZY)
    private List<Specification> specifications;

    public Long getSpecificationTypeID() {
        return specificationTypeID;
    }

    public void setSpecificationTypeID(Long specificationTypeID) {
        this.specificationTypeID = specificationTypeID;
    }

    public String getSpecificationTypeName() {
        return specificationTypeName;
    }

    public void setSpecificationTypeName(String specificationTypeName) {
        this.specificationTypeName = specificationTypeName;
    }

    public Long getTypeID() {
        return typeID;
    }

    public void setTypeID(Long typeID) {
        this.typeID = typeID;
    }

    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.setValue(type);
    }

    public List<Specification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<Specification> specifications) {
        this.specifications = specifications;
    }
}
