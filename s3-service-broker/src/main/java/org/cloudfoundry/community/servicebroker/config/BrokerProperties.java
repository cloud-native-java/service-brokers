package org.cloudfoundry.community.servicebroker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "broker")
public class BrokerProperties {

    private String providerDisplayName;
    private String documentationUrl;
    private String supportUrl;
    private String displayName;
    private String longDescription;
    private String imageUrl;

    @NestedConfigurationProperty
    private BasicPlan basicPlan;

    @NestedConfigurationProperty
    private Definition definition;

    public static class BasicPlan {
        private String id;
        private String name;
        private String description;
        private Boolean free;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getFree() {
            return free;
        }

        public void setFree(Boolean free) {
            this.free = free;
        }

        @Override
        public String toString() {
            return "BasicPlan{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", free=" + free +
                    '}';
        }
    }

    public static class Definition {
        private String id;
        private String name;
        private String description;
        private Boolean bindable;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getBindable() {
            return bindable;
        }

        public void setBindable(Boolean bindable) {
            this.bindable = bindable;
        }

        @Override
        public String toString() {
            return "Definition{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", bindable=" + bindable +
                    '}';
        }
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BasicPlan getBasicPlan() {
        return basicPlan;
    }

    public void setBasicPlan(BasicPlan basicPlan) {
        this.basicPlan = basicPlan;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "BrokerProperties{" +
                "providerDisplayName='" + providerDisplayName + '\'' +
                ", documentationUrl='" + documentationUrl + '\'' +
                ", supportUrl='" + supportUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", basicPlan=" + basicPlan +
                ", definition=" + definition +
                '}';
    }
}
