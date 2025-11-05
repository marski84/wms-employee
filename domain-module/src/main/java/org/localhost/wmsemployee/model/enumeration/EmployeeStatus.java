package org.localhost.wmsemployee.model.enumeration;

import lombok.Getter;

@Getter
public enum EmployeeStatus {
    PROBATION(0),
    ACTIVE(1),
    HOLIDAY(2),
    SICK_LEAVE(3),
    OFF_WORK(4),
    SUSPENDED(5),
    TERMINATED(6),
    REGISTERED(7);


    private final int employeeStatusId;
    EmployeeStatus(int statusId) {
        employeeStatusId = statusId;
    }
}
