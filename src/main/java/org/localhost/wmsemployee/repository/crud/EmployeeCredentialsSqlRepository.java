package org.localhost.wmsemployee.repository.crud;

import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeCredentialsSqlRepository extends CrudRepository<EmployeeCredentials, Long> {
}
