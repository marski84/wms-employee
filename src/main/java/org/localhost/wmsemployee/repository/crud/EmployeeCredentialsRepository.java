package org.localhost.wmsemployee.repository.crud;

import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.model.EmployeeCredentials;
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
