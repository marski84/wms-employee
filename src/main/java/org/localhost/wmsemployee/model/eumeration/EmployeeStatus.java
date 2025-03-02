package org.localhost.wmsemployee.model.eumeration;

import lombok.Getter;

@Getter
public enum EmployeeStatus {
    PROBATION(0),
    ACTIVE(1),
    HOLIDAY(2),
    MEDICAL_LEAVE(3),
    OFF_WORK(4),
    SUSPENDED(5),
    TERMINATED(6);


    private int employeeStatusId;
    EmployeeStatus(int statusId) {
        employeeStatusId = statusId;
    }
}
