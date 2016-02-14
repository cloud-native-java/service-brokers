package org.cloudfoundry.community.servicebroker.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.repositories.ServiceInstanceBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceInstanceBindingServiceImpl implements ServiceInstanceBindingService {

    Log log = LogFactory.getLog(ServiceInstanceBindingService.class);

    private ServiceInstanceBindingRepository serviceInstanceBindingRepository;
    private ServiceInstanceService serviceInstanceService;

    @Autowired
    public ServiceInstanceBindingServiceImpl(ServiceInstanceBindingRepository serviceInstanceBindingRepository, ServiceInstanceService serviceInstanceService) {
        this.serviceInstanceBindingRepository = serviceInstanceBindingRepository;
        this.serviceInstanceService = serviceInstanceService;
    }

    /**
     * Create a new binding to a service instance.
     *
     * @param createServiceInstanceBindingRequest containing parameters sent from Cloud Controller
     * @return The newly created ServiceInstanceBinding
     * @throws ServiceInstanceBindingExistsException if the same binding already exists
     * @throws ServiceBrokerException                on internal failure
     */
    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest) throws ServiceInstanceBindingExistsException, ServiceBrokerException {

        ServiceInstanceBinding serviceInstanceBinding = serviceInstanceBindingRepository.findOne(createServiceInstanceBindingRequest.getBindingId());

        if (serviceInstanceBinding != null)
            throw new ServiceInstanceBindingExistsException(serviceInstanceBinding);

        ServiceInstance serviceInstance = null;

        // Get service instance
        try {
            serviceInstance = serviceInstanceService.getServiceInstance(createServiceInstanceBindingRequest.getServiceInstanceId());
        } catch (ServiceInstanceDoesNotExistException ex) {
            log.error("Could not get service instance {}", ex);
            throw new ServiceBrokerException(ex);
        }

        // Test credentials
        Map<String, String> credentials;
        credentials = new HashMap<>();
        credentials.put("userName", serviceInstance.getCredential().getUserName());
        credentials.put("accessKeyId", serviceInstance.getCredential().getAccessKeyId());
        credentials.put("secretAccessKey", serviceInstance.getCredential().getSecretAccessKey());

        serviceInstanceBinding = new ServiceInstanceBinding(createServiceInstanceBindingRequest.getBindingId(),
                createServiceInstanceBindingRequest.getServiceInstanceId(),
                credentials, null, createServiceInstanceBindingRequest.getAppGuid());

        try {
            serviceInstanceBinding = serviceInstanceBindingRepository.save(serviceInstanceBinding);
        } catch (Exception ex) {
            log.error(ex);
            throw new ServiceBrokerException(ex);
        }

        return serviceInstanceBinding;
    }

    /**
     * Delete the service instance binding. If a binding doesn't exist,
     * return null.
     *
     * @param deleteServiceInstanceBindingRequest containing parameters sent from Cloud Controller
     * @return The deleted ServiceInstanceBinding or null if one does not exist
     * @throws ServiceBrokerException on internal failure
     */
    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest) throws ServiceBrokerException {
        ServiceInstanceBinding serviceInstanceBinding;

        if (!serviceInstanceBindingRepository.exists(deleteServiceInstanceBindingRequest.getBindingId())) {
            throw new ServiceBrokerException(String.format("Service instance binding does not exist: %s", deleteServiceInstanceBindingRequest.getBindingId()));
        } else {
            serviceInstanceBinding = serviceInstanceBindingRepository.findOne(deleteServiceInstanceBindingRequest.getBindingId());
        }

        try {
            serviceInstanceBindingRepository.delete(deleteServiceInstanceBindingRequest.getBindingId());
        } catch (Exception ex) {
            log.error(ex);
        }

        return serviceInstanceBinding;
    }
}
