package org.cloudfoundry.community.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * A request sent by the cloud controller to update an instance of a service.
 * 
 */
public class UpdateServiceInstanceRequest {

	@JsonProperty("plan_id")
	private String planId;
	private Map<String, Object> parameters;
	
	@JsonIgnore
	private String serviceInstanceId;

	public UpdateServiceInstanceRequest() {
	}
	
	public UpdateServiceInstanceRequest(String planId) {
		this.planId = planId;
	}

	public UpdateServiceInstanceRequest(String planId, Map<String, Object> parameters) {
		this(planId);
		this.parameters = parameters;
	}

	public String getPlanId() {
		return planId;
	}
	
	public String getServiceInstanceId() { 
		return serviceInstanceId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public UpdateServiceInstanceRequest withInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId; 
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdateServiceInstanceRequest that = (UpdateServiceInstanceRequest) o;
		return Objects.equals(planId, that.planId) &&
				Objects.equals(parameters, that.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(planId, parameters);
	}
}
