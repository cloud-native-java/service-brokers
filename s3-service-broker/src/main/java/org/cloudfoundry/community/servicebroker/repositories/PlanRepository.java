package org.cloudfoundry.community.servicebroker.repositories;

import org.cloudfoundry.community.servicebroker.catalog.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, String> {
}
