package cnj;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class ServiceInstanceBinding {

	@Id
	private String id;

	private String serviceInstanceId, syslogDrainUrl, appGuid;

	public ServiceInstanceBinding(String id,
	                              String serviceInstanceId,
	                              String syslogDrainUrl,
	                              String appGuid) {
		this.id = id;
		this.serviceInstanceId = serviceInstanceId;
		this.syslogDrainUrl = syslogDrainUrl;
		this.appGuid = appGuid;
	}
}