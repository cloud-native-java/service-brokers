package org.cloudfoundry.community.servicebroker.model.fixture;

import org.cloudfoundry.community.servicebroker.catalog.Plan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlanFixture {

	public static HashSet<Plan> getAllPlans() {
		List<Plan> plans = new ArrayList<Plan>();
		plans.add(getPlanOne());
		plans.add(getPlanTwo());
		return new HashSet<>(plans);
	}
		
	public static Plan getPlanOne() {
		return new Plan("plan-one-id", "Plan One", "Description for Plan One");
	}
	
	public static Plan getPlanTwo() {
		return new Plan("plan-two-id", "Plan Two", "Description for Plan Two");
	}
	
}
