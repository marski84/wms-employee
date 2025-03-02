package org.localhost.wmsemployee.service.impl;
import jakarta.validation.Valid;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Override
    public Employee registerNewEmployee(Employee employee) {
        return new Employee();
    }
}
