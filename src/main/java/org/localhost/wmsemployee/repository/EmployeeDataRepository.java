package org.localhost.wmsemployee.repository;

import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeDataRepository extends CrudRepository<EmployeeData, Long> {
}
