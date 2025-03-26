package org.localhost.wmsemployee.repository.crud;

import org.localhost.wmsemployee.model.EmployeeContactDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeContactDetailsSqlRepository extends CrudRepository<EmployeeContactDetails, Long> {
}
