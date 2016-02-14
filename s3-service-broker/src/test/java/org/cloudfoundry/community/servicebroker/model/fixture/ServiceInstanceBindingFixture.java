package org.cloudfoundry.community.servicebroker.model.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServiceInstanceBindingFixture {

	public static ServiceInstanceBinding getServiceInstanceBinding() {
		ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
		return new ServiceInstanceBinding(
				getServiceInstanceBindingId(),
				instance.getServiceInstanceId(),
				getCredentials(),
				getSysLogDrainUrl(),
				getAppGuid()
		);
	}

	public static String getServiceInstanceBindingId() {
		return "service_instance_binding_id";
	}
	
	public static Map<String,String> getCredentials() {
		Map<String,String> credentials;
        credentials = new HashMap<>();
        credentials.put("uri","uri");
		credentials.put("username", "username");
		credentials.put("password", "password");
		return credentials;
	}
	
	public static String getSysLogDrainUrl() {
		return "syslog_drain_url";
	}
	
	public static String getAppGuid() {
		return "app_guid";
	}
	
	public static CreateServiceInstanceBindingRequest getServiceInstanceBindingRequest() {
		return new CreateServiceInstanceBindingRequest(
				ServiceFixture.getService().getId(), 
				PlanFixture.getPlanOne().getId(),
				getAppGuid(),
				ParametersFixture.getParameters()
		); 	
	}
	
	public static String getServiceInstanceBindingRequestJson() throws IOException {
		 ObjectMapper mapper = new ObjectMapper();
		 return mapper.writeValueAsString(getServiceInstanceBindingRequest());
	}
	
}
