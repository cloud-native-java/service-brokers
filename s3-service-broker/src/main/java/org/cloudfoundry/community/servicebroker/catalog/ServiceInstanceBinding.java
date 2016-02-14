package org.cloudfoundry.community.servicebroker.catalog;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A binding to a service instance
 *
 * @author sgreenberg@gopivotal.com
 */
@Entity
public class ServiceInstanceBinding {

    @Id
    private String id;
    private String serviceInstanceId;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="example_attributes", joinColumns=@JoinColumn(name="example_id"))
    private Map<String, String> credentials = new HashMap<String, String>();
    private String syslogDrainUrl;
    private String appGuid;

    public ServiceInstanceBinding() {
        id = UUID.randomUUID().toString();
    }

    public ServiceInstanceBinding(String id,
                                  String serviceInstanceId,
                                  Map<String, String> credentials,
                                  String syslogDrainUrl, String appGuid) {
        this.id = id;
        this.serviceInstanceId = serviceInstanceId;
        this.credentials = credentials;
        this.syslogDrainUrl = syslogDrainUrl;
        this.appGuid = appGuid;
    }

    public String getId() {
        return id;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    private void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public String getAppGuid() {
        return appGuid;
    }

}
