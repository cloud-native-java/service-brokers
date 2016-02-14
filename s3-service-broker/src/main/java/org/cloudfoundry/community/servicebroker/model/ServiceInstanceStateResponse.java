package org.cloudfoundry.community.servicebroker.model;

import java.io.Serializable;

public class ServiceInstanceStateResponse implements Serializable {

    private String state;
    private String description;

    public ServiceInstanceStateResponse() {
    }

    public ServiceInstanceStateResponse(String state, String description) {
        this.state = state;
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
