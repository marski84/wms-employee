package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.repository.crud.EmployeeCredentialsSqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeCredentialsRepository {
    private final EmployeeCredentialsSqlRepository employeeCredentialsSqlRepository;

    public EmployeeCredentialsRepository(EmployeeCredentialsSqlRepository employeeCredentialsSqlRepository) {
        this.employeeCredentialsSqlRepository = employeeCredentialsSqlRepository;
    }
}
