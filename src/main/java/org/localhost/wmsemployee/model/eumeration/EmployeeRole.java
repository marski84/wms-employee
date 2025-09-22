package org.localhost.wmsemployee.model.eumeration;

import lombok.Getter;

@Getter
public enum EmployeeRole {
    EMPLOYEE(1, "EMPLOYEE"),
    MANAGER(2, "MANAGER"),
    ADMIN(3, "ADMIN"),
    HR(4, "HR");

    private final int roleId;
    private final String roleName;

    EmployeeRole(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
}
