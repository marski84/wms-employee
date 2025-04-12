package org.localhost.wmsemployee.repository.crud;

import org.springframework.stereotype.Repository;

@Repository
public class EmployeeContactDetailsRepository {
    private final EmployeeContactDetailsCrudRepository employeeContactDetailsCrudRepository;

    public EmployeeContactDetailsRepository(EmployeeContactDetailsCrudRepository employeeContactDetailsCrudRepository) {
        this.employeeContactDetailsCrudRepository = employeeContactDetailsCrudRepository;
    }
}
