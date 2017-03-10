package cnj;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
class ServiceInstance {

	@Id
	private String id;
	private String serviceDefinitionId;
	private String planId;
	private String organizationGuid;
	private String spaceGuid;
	private String dashboardUrl;
	private String username, accessKeyId, secretAccessKey;

	public ServiceInstance(CreateServiceInstanceRequest request) {
		this.serviceDefinitionId = request.getServiceDefinitionId();
		this.planId = request.getPlanId();
		this.organizationGuid = request.getOrganizationGuid();
		this.spaceGuid = request.getSpaceGuid();
		this.id = request.getServiceInstanceId();
	}

	public ServiceInstance(DeleteServiceInstanceRequest request) {
		this.id = request.getServiceInstanceId();
		this.planId = request.getPlanId();
		this.serviceDefinitionId = request.getServiceDefinitionId();
	}

	public ServiceInstance(UpdateServiceInstanceRequest request) {
		this.id = request.getServiceInstanceId();
		this.planId = request.getPlanId();
	}
}
