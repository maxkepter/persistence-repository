package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.ManyToOne;
import com.example.persistence_repository.persistence.annotation.OneToMany;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;
import java.util.List;

@Entity(tableName = "Type")
public class Type {
    @Key
    @Column(name = "TypeID", type = "BIGINT")
    private Long typeID;

    @Column(name = "TypeName", length = 100, nullable = false)
    private String typeName;

    @Column(name = "TypeImage", length = 255)
    private String typeImage;

    @Column(name = "CategoryID", type = "BIGINT", nullable = false)
    private Long categoryID;

    @ManyToOne(joinColumn = "CategoryID", fetch = FetchMode.EAGER)
    private LazyReference<Category> category;

    @OneToMany(mappedBy = "typeID", joinColumn = "TypeID", fetch = FetchMode.LAZY)
    private List<Product> products;

    @OneToMany(mappedBy = "typeID", joinColumn = "TypeID", fetch = FetchMode.LAZY)
    private List<SpecificationType> specificationTypes;

    public Long getTypeID() {
        return typeID;
    }

    public void setTypeID(Long typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeImage() {
        return typeImage;
    }

    public void setTypeImage(String typeImage) {
        this.typeImage = typeImage;
    }

    public Long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public Category getCategory() {
        return category.get();
    }

    public void setCategory(Category category) {
        this.category.setValue(category);
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<SpecificationType> getSpecificationTypes() {
        return specificationTypes;
    }

    public void setSpecificationTypes(List<SpecificationType> specificationTypes) {
        this.specificationTypes = specificationTypes;
    }
}
