package org.cloudfoundry.community.servicebroker.repositories;

import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.springframework.data.repository.CrudRepository;

public interface ServiceDefinitionRepository extends CrudRepository<ServiceDefinition, String> {
}
