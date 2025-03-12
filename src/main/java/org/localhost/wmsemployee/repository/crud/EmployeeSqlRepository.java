package org.localhost.wmsemployee.repository.crud;

import org.localhost.wmsemployee.model.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSqlRepository extends CrudRepository<Employee, Long> {

}
