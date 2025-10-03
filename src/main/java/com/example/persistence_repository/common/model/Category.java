package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.OneToMany;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.util.List;

@Entity(tableName = "Category")
public class Category {
    @Key
    @Column(name = "CategoryID", type = "BIGINT")
    private Long categoryID;

    @Column(name = "CategoryName", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "CategoryImage", length = 255)
    private String categoryImage;

    @OneToMany(mappedBy = "categoryID", joinColumn = "CategoryID", fetch = FetchMode.LAZY)
    private List<Type> types;

    public Long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }
}
