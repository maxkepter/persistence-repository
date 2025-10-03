package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;
import com.example.persistence_repository.persistence.annotation.ManyToOne;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

@Entity(tableName = "RoleFeature")
public class RoleFeature {
    // Surrogate key instead of composite
    @Key
    @Column(name = "RoleFeatureID", type = "BIGINT")
    private Long roleFeatureID;

    @Column(name = "RoleID", type = "BIGINT", nullable = false)
    private Long roleID;

    @Column(name = "FeatureID", type = "BIGINT", nullable = false)
    private Long featureID;

    @ManyToOne(joinColumn = "RoleID", fetch = FetchMode.LAZY)
    private LazyReference<Role> role;

    @ManyToOne(joinColumn = "FeatureID", fetch = FetchMode.LAZY)
    private LazyReference<Feature> feature;

    public Long getRoleFeatureID() {
        return roleFeatureID;
    }

    public void setRoleFeatureID(Long roleFeatureID) {
        this.roleFeatureID = roleFeatureID;
    }

    public Long getRoleID() {
        return roleID;
    }

    public void setRoleID(Long roleID) {
        this.roleID = roleID;
    }

    public Long getFeatureID() {
        return featureID;
    }

    public void setFeatureID(Long featureID) {
        this.featureID = featureID;
    }

    public Role getRole() {
        return role.get();
    }

    public void setRole(Role role) {
        this.role.setValue(role);
    }

    public Feature getFeature() {
        return feature.get();
    }

    public void setFeature(Feature feature) {
        this.feature.setValue(feature);
    }
}
