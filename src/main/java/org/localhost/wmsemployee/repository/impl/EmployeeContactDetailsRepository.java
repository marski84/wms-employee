package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.repository.crud.EmployeeContactDetailsSqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeContactDetailsRepository {
    private final EmployeeContactDetailsSqlRepository employeeContactDetailsSqlRepository;

    public EmployeeContactDetailsRepository(EmployeeContactDetailsSqlRepository employeeContactDetailsSqlRepository) {
        this.employeeContactDetailsSqlRepository = employeeContactDetailsSqlRepository;
    }
}
