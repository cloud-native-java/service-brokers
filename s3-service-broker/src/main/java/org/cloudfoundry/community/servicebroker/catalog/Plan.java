package org.cloudfoundry.community.servicebroker.catalog;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Map;
import java.util.UUID;

/**
 * A service plan available for a ServiceDefinition
 *
 * @author sgreenberg@gopivotal.com
 * @author kbastani
 */
@Entity
public class Plan {

    @Id
    private String id;
    private String name;
    private String description;

    @Transient
    private Map<String, Object> metadata;

    private boolean free;

    public Plan() {
        id = UUID.randomUUID().toString();
    }

    public Plan(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public Plan(String id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public Plan(String id, String name, String description, Map<String, Object> metadata) {
        this(id, name, description);
        setMetadata(metadata);
    }

    public Plan(String id, String name, String description, Map<String, Object> metadata, boolean free) {
        this(id, name, description, metadata);
        this.free = free;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    private void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;

        if (free != plan.free) return false;
        if (id != null ? !id.equals(plan.id) : plan.id != null) return false;
        if (name != null ? !name.equals(plan.name) : plan.name != null) return false;
        if (description != null ? !description.equals(plan.description) : plan.description != null) return false;
        return !(metadata != null ? !metadata.equals(plan.metadata) : plan.metadata != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (free ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", metadata=" + metadata +
                ", free=" + free +
                '}';
    }

}
