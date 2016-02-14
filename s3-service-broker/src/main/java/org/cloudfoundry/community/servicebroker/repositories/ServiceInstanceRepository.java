package org.cloudfoundry.community.servicebroker.repositories;

import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInstanceRepository extends JpaRepository<ServiceInstance, String> {
}
