package org.localhost.wmsemployee.service.employee;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.exceptions.NoValidDtoException;
import org.localhost.wmsemployee.repository.EmployeeDataRepository;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class EmployeeDataService {
    private final EmployeeDataRepository employeeDataRepository;

    public EmployeeDataService(EmployeeDataRepository employeeDataRepository) {
        this.employeeDataRepository = employeeDataRepository;
    }

    /**
     * Saves employee data from Auth0 registration DTO to the database.
     *
     * @param employeeDto The Auth0 registration DTO containing employee data
     * @throws NoValidDtoException if the provided DTO is null
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(Auth0RegistrationDto employeeDto) {
        if (employeeDto == null) {
            log.error("Employee data save attempt failed, Auth0RegistrationDto is null");
            throw new NoValidDtoException();
        }

        EmployeeData employeeData = EmployeeData.fromAuth0Dto(employeeDto);

        employeeDataRepository.save(employeeData);
        log.info("Employee data saved successfully for username: {}", employeeData.getUsername());
    }

    /**
     * Find employee data from db by email.
     *
     * @param email user email
     * @return Optional containing the employee data if found, empty Optional otherwise
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeData> findByEmail(String email) {
        return employeeDataRepository.findByEmail(email);
    }

    /**
     * Find employee data from db by username.
     *
     * @param username user username
     * @return Optional containing the employee data if found, empty Optional otherwise
     */
    @Transactional(readOnly = true)
    public Optional<EmployeeData> findByUsername(String username) {
        return employeeDataRepository.findByUsername(username);
    }
}
