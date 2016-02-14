package org.cloudfoundry.community.servicebroker.catalog;

import org.cloudfoundry.community.servicebroker.repositories.ServiceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The catalog of services offered by this broker.
 *
 * @author sgreenberg@gopivotal.com
 * @author kbastani
 */
@Service
public class Catalog implements Serializable {

    private List<ServiceDefinition> services = new ArrayList<>();

    @Autowired
    ServiceDefinitionRepository serviceDefinitionRepository;

    public Catalog() {
    }

    public Catalog(List<ServiceDefinition> services) {
        this.setServices(services);
    }

    public List<ServiceDefinition> getServices() {
        if (services.size() == 0) {
            services = new ArrayList<>((Collection<ServiceDefinition>) serviceDefinitionRepository.findAll());
        }
        return services;
    }

    private void setServices(List<ServiceDefinition> services) {
        if (services == null) {
            this.services = new ArrayList<>((Collection<ServiceDefinition>) serviceDefinitionRepository.findAll());
        } else {
            this.services = services;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Catalog catalog = (Catalog) o;

        return !(services != null ? !services.equals(catalog.services) : catalog.services != null);

    }

    @Override
    public int hashCode() {
        return services != null ? services.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Catalog{" +
                "services=" + services +
                '}';
    }
}
