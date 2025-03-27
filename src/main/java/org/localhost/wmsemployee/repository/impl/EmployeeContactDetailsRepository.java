package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.repository.crud.EmployeeContactDetailsCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeContactDetailsRepository {
    private final EmployeeContactDetailsCrudRepository employeeContactDetailsCrudRepository;

    public EmployeeContactDetailsRepository(EmployeeContactDetailsCrudRepository employeeContactDetailsCrudRepository) {
        this.employeeContactDetailsCrudRepository = employeeContactDetailsCrudRepository;
    }
}
