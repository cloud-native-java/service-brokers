package org.cloudfoundry.community.servicebroker.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudfoundry.community.servicebroker.model.ServiceMetaData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * A service offered by this broker.
 *
 * @author sgreenberg@gopivotal.com
 */
@Entity
public class ServiceDefinition implements Serializable {

    @Id
    private String id;

    private String name;

    private String description;

    private boolean bindable;

    @JsonProperty("plan_updateable")
    private boolean planUpdateable;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Collection<Plan> plans = new HashSet<>();

    private HashSet<String> tags = null;

    private HashSet<String> requires;

    @JsonProperty("dashboard_client")
    private DashboardClient dashboardClient;

    @Transient
    @JsonProperty("metadata")
    private Map<String,Object> metadata = ServiceMetaData.META_DATA;

    public ServiceDefinition() {
        this.id = UUID.randomUUID().toString();
    }

    public ServiceDefinition(String name, String description, boolean bindable, Collection<Plan> plans) {
        this();
        this.name = name;
        this.description = description;
        this.bindable = bindable;
        this.plans = plans;
    }

    public ServiceDefinition(String id, String name, String description, boolean bindable, Collection<Plan> plans) {
        this(name, description, bindable, plans);
        this.id = id;
    }

    public ServiceDefinition(String id, String name, String description, boolean bindable, Collection<Plan> plans, Map<String, Object> metadata) {
        this(id, name, description, bindable, plans);
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBindable() {
        return bindable;
    }

    public boolean isPlanUpdateable() {
        return planUpdateable;
    }

    public void setPlanUpdateable(boolean planUpdateable) {
        this.planUpdateable = planUpdateable;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBindable(boolean bindable) {
        this.bindable = bindable;
    }

    public Collection<Plan> getPlans() {
        return plans;
    }

    public void setPlans(Collection<Plan> plans) {
        this.plans = plans;
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public void setTags(HashSet<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public HashSet<String> getRequires() {
        return requires;
    }

    public void setRequires(HashSet<String> requires) {
        this.requires = requires;
    }

    public DashboardClient getDashboardClient() {
        return dashboardClient;
    }

    public void setDashboardClient(DashboardClient dashboardClient) {
        this.dashboardClient = dashboardClient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDefinition that = (ServiceDefinition) o;

        if (bindable != that.bindable) return false;
        if (planUpdateable != that.planUpdateable) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (plans != null ? !plans.equals(that.plans) : that.plans != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
        if (requires != null ? !requires.equals(that.requires) : that.requires != null) return false;
        return !(dashboardClient != null ? !dashboardClient.equals(that.dashboardClient) : that.dashboardClient != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (bindable ? 1 : 0);
        result = 31 * result + (planUpdateable ? 1 : 0);
        result = 31 * result + (plans != null ? plans.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (requires != null ? requires.hashCode() : 0);
        result = 31 * result + (dashboardClient != null ? dashboardClient.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", bindable=" + bindable +
                ", planUpdateable=" + planUpdateable +
                ", plans=" + plans +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", requires=" + requires +
                ", dashboardClient=" + dashboardClient +
                '}';
    }
}
