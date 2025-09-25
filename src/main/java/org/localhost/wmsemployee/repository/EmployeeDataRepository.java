package org.localhost.wmsemployee.repository;

import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeDataRepository extends CrudRepository<EmployeeData, Long> {
    Optional<EmployeeData> findByEmail(String email);

    Optional<EmployeeData> findByUsername(String username);
}
