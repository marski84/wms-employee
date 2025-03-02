package org.localhost.wmsemployee.model.eumeration;

import lombok.Getter;

@Getter
public enum EmployeeRole {
    EMPLOYEE(1),
    MANAGER(2),
    ADMIN(3),
    HR(4);

    private final int roleId;

    EmployeeRole(int roleId) {
        this.roleId = roleId;
    }
}
