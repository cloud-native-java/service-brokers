package org.cloudfoundry.community.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;

import java.util.Map;

/**
 * The response sent to the cloud controller when a bind
 * request is successful.
 * 
 * @author sgreenberg@gopivotal.com
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
public class ServiceInstanceBindingResponse {

	ServiceInstanceBinding binding;
	
	public ServiceInstanceBindingResponse() {}
	
	public ServiceInstanceBindingResponse(ServiceInstanceBinding binding) {
		this.binding = binding;
	}

	@JsonProperty("credentials")
	public Map<String, String> getCredentials() {
		return binding.getCredentials();
	}

	@JsonProperty("syslog_drain_url")
	public String getSyslogDrainUrl() {
		return binding.getSyslogDrainUrl();
	}
	
}
