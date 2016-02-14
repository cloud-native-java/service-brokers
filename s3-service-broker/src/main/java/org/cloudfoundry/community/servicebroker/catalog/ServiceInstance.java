package org.cloudfoundry.community.servicebroker.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;


/**
 * An instance of a ServiceDefinition.
 *
 * @author sgreenberg@gopivotal.com
 * @author kbastani
 */
@Entity
public class ServiceInstance {

    @Id
    @JsonProperty("service_instance_id")
    private String serviceInstanceId;
    
    @JsonProperty("service_id")
    private String serviceDefinitionId;
    
    @JsonProperty("plan_id")
    private String planId;

    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Credential credential;

    private ServiceInstance() {
    }

    /**
     * Create a ServiceInstance from a create request. If fields
     * are not present in the request they will remain null in the
     * ServiceInstance.
     *
     * @param request containing details of ServiceInstance
     */
    public ServiceInstance(CreateServiceInstanceRequest request) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.organizationGuid = request.getOrganizationGuid();
        this.spaceGuid = request.getSpaceGuid();
        this.serviceInstanceId = request.getServiceInstanceId();
    }

    /**
     * Create a ServiceInstance from a delete request. If fields
     * are not present in the request they will remain null in the
     * ServiceInstance.
     *
     * @param request containing details of ServiceInstance
     */
    public ServiceInstance(DeleteServiceInstanceRequest request) {
        this.serviceInstanceId = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceDefinitionId = request.getServiceId();
    }

    /**
     * Create a service instance from a delete request. If fields
     * are not present in the request they will remain null in the
     * ServiceInstance.
     *
     * @param request containing details of ServiceInstance
     */
    public ServiceInstance(UpdateServiceInstanceRequest request) {
        request.getPlanId();
        request.getServiceInstanceId();
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getOrganizationGuid() {
        return organizationGuid;
    }

    public String getSpaceGuid() {
        return spaceGuid;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public ServiceInstance withDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
        return this;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

}
