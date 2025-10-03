package com.example.persistence_repository.common.model;

import com.example.persistence_repository.persistence.annotation.Column;
import com.example.persistence_repository.persistence.annotation.Entity;
import com.example.persistence_repository.persistence.annotation.Key;

@Entity(tableName = "Role")
public class Role {
    @Key
    @Column(name = "RoleID", type = "BIGINT")
    private Long roleID;

    @Column(name = "RoleName", length = 50, unique = true)
    private String roleName;

    public Long getRoleID() {
        return roleID;
    }

    public void setRoleID(Long roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
