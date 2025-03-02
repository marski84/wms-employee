package org.localhost.wmsemployee.service;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.model.Employee;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeService {
        Employee registerNewEmployee(@Valid Employee employee);

    }
