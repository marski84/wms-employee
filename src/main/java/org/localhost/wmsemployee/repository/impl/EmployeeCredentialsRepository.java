package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.localhost.wmsemployee.repository.crud.EmployeeCredentialsCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeCredentialsRepository {
    private final EmployeeCredentialsCrudRepository employeeCredentialsCrudRepository;

    public EmployeeCredentialsRepository(EmployeeCredentialsCrudRepository employeeCredentialsCrudRepository) {
        this.employeeCredentialsCrudRepository = employeeCredentialsCrudRepository;
    }

    public EmployeeCredentials findById(Long userId) {
        return employeeCredentialsCrudRepository.findById(userId).orElseThrow(EmployeeNotFoundException::new);
    }

    public void save(EmployeeCredentials employeeCredentials) {
        employeeCredentialsCrudRepository.save(employeeCredentials);
    }
}
