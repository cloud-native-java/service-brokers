package org.cloudfoundry.community.servicebroker.service;

import org.cloudfoundry.community.servicebroker.catalog.Catalog;
import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.repositories.ServiceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An implementation of the CatalogService that gets the catalog injected (ie configure 
 * in spring config)
 * 
 * @author sgreenberg@gopivotal.com
 *
 */
public class BeanCatalogService implements CatalogService {

	private Catalog catalog;
    private ServiceDefinitionRepository serviceDefinitionRepository;

	@Autowired
	public BeanCatalogService(Catalog catalog, ServiceDefinitionRepository serviceDefinitionRepository) {
		this.catalog = catalog;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
	}

	@Override
	public Catalog getCatalog() {
        catalog.getServices();
		return catalog;
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) {
		return serviceDefinitionRepository.findOne(serviceId);
	}

}
