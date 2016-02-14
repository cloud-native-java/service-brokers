package org.cloudfoundry.community.servicebroker.repositories;

import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInstanceBindingRepository extends JpaRepository<ServiceInstanceBinding, String> {
}
