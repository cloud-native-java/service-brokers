package org.cloudfoundry.community.servicebroker.service.impl;

import org.cloudfoundry.community.servicebroker.ServiceBrokerApplication;
import org.cloudfoundry.community.servicebroker.catalog.Catalog;
import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.repositories.ServiceDefinitionRepository;
import org.cloudfoundry.community.servicebroker.service.BeanCatalogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ServiceBrokerApplication.class)
@WebIntegrationTest
@ActiveProfiles("test")
public class BeanCatalogServiceTest {

    @Autowired
	private BeanCatalogService service;

    @Autowired
    private ServiceDefinitionRepository serviceDefinitionRepository;

	private Catalog catalog;
	private ServiceDefinition serviceDefinition;

	private static final String SVC_DEF_ID = "svc-def-id";
	
	@Before
	public void setup() {
		serviceDefinition = new ServiceDefinition(SVC_DEF_ID, "Name", "Description", true, new ArrayList<>());
		List<ServiceDefinition> defs = new ArrayList<ServiceDefinition>();
		defs.add(serviceDefinition);
		catalog = new Catalog(defs);
        serviceDefinitionRepository.save(serviceDefinition);
	}
	
	@Test
	public void catalogIsReturnedSuccessfully() {
		assertEquals(catalog, service.getCatalog());
	}
	
	@Test 
	public void itFindsServiceDefinition() {
		assertEquals(serviceDefinition, service.getServiceDefinition(SVC_DEF_ID));
	}

	@Test 
	public void itDoesNotFindServiceDefinition() {
		assertNull(service.getServiceDefinition("NOT_THERE"));
	}
	
}
