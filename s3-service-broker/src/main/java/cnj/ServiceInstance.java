package cnj;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
// <1>
@Data
// <2>
@NoArgsConstructor
@ToString
@EqualsAndHashCode
class ServiceInstance {

 @Id
 private String id;

 private String serviceDefinitionId;

 private String planId;

 private String organizationGuid;

 private String spaceGuid;

 private String dashboardUrl;

 private String username, accessKeyId, secretAccessKey;

 // <3>
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
