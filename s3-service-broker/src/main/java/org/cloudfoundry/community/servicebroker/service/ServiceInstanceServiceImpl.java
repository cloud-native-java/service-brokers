package org.cloudfoundry.community.servicebroker.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.community.servicebroker.catalog.Credential;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.S3User;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.repositories.PlanRepository;
import org.cloudfoundry.community.servicebroker.repositories.ServiceInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceInstanceServiceImpl implements ServiceInstanceService {

    private ServiceInstanceRepository serviceInstanceRepository;
    private PlanRepository planRepository;
    private S3Service s3Service;

    Log log = LogFactory.getLog(ServiceInstanceService.class);

    @Autowired
    public ServiceInstanceServiceImpl(ServiceInstanceRepository serviceInstanceRepository, PlanRepository planRepository, S3Service s3Service) {
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.planRepository = planRepository;
        this.s3Service = s3Service;
    }

    /**
     * Create a new instance of a service
     *
     * @param createServiceInstanceRequest containing the parameters from CloudController
     * @return The newly created ServiceInstance
     * @throws ServiceInstanceExistsException if the service instance already exists.
     * @throws ServiceBrokerException         if something goes wrong internally
     */
    @Override
    public ServiceInstance createServiceInstance(
            CreateServiceInstanceRequest createServiceInstanceRequest)
            throws ServiceInstanceExistsException, ServiceBrokerException {

        ServiceInstance serviceInstance = new ServiceInstance(createServiceInstanceRequest);

        if (serviceInstanceRepository.exists(serviceInstance.getServiceInstanceId()))
            throw new ServiceInstanceExistsException(serviceInstance);

        try {
            S3User user = s3Service.createBucket(
                    serviceInstance.getServiceInstanceId());

            serviceInstance.setCredential(
                    new Credential(user.getCreateUserResult().getUser().getUserName(),
                            user.getAccessKeyId(), user.getAccessKeySecret()));

            serviceInstance = serviceInstanceRepository.save(serviceInstance);
        } catch (Exception ex) {
            log.error(ex);
            throw new ServiceBrokerException(ex);
        }

        return serviceInstance;
    }

    /**
     * @param serviceInstanceId The id of the serviceInstance
     * @return The ServiceInstance with the given id or null if one does not exist
     */
    @Override
    public ServiceInstance getServiceInstance(String serviceInstanceId) throws ServiceInstanceDoesNotExistException {
        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = serviceInstanceRepository.findOne(serviceInstanceId);
        } catch (Exception ex) {
            log.error(ex);
        }

        if (serviceInstance == null) {
            throw new ServiceInstanceDoesNotExistException(serviceInstanceId);
        }

        return serviceInstance;
    }

    /**
     * Delete and return the instance if it exists.
     *
     * @param deleteServiceInstanceRequest containing pertinent information for deleting the service.
     * @return The deleted ServiceInstance or null if one did not exist.
     * @throws ServiceBrokerException is something goes wrong internally
     */
    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest deleteServiceInstanceRequest) throws ServiceBrokerException, ServiceInstanceDoesNotExistException {

        ServiceInstance serviceInstance;

        if (!serviceInstanceRepository.exists(deleteServiceInstanceRequest.getServiceInstanceId())) {
            throw new ServiceInstanceDoesNotExistException(deleteServiceInstanceRequest.getServiceInstanceId());
        } else {
            serviceInstance = serviceInstanceRepository.findOne(deleteServiceInstanceRequest.getServiceInstanceId());
        }


        // Delete service broker
        if (!s3Service.deleteServiceInstanceBucket(serviceInstance.getServiceInstanceId(),
                serviceInstance.getCredential().getAccessKeyId(),
                serviceInstance.getCredential().getUserName())) {
            log.error("Could not delete the S3 bucket for the service instance");
        }

        try {
            serviceInstanceRepository.delete(deleteServiceInstanceRequest.getServiceInstanceId());
        } catch (Exception ex) {
            log.error(ex);
            throw new ServiceBrokerException(ex);
        }

        return serviceInstance;
    }

    /**
     * Update a service instance. Only modification of service plan is supported.
     *
     * @param updateServiceInstanceRequest detailing the request parameters
     * @return The updated serviceInstance
     * @throws ServiceInstanceUpdateNotSupportedException if particular plan change is not supported
     *                                                    or if the request can not currently be fulfilled due to the state of the instance.
     * @throws ServiceInstanceDoesNotExistException       if the service instance does not exist
     * @throws ServiceBrokerException                     if something goes wrong internally
     */
    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest updateServiceInstanceRequest) throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
        ServiceInstance serviceInstance;

        if (!serviceInstanceRepository.exists(updateServiceInstanceRequest.getServiceInstanceId())) {
            throw new ServiceInstanceDoesNotExistException(updateServiceInstanceRequest.getServiceInstanceId());
        } else {
            serviceInstance = serviceInstanceRepository.getOne(updateServiceInstanceRequest.getServiceInstanceId());
        }

        if (planRepository.exists(serviceInstance.getPlanId())) {
            serviceInstance.setPlanId(updateServiceInstanceRequest.getPlanId());
            serviceInstance = serviceInstanceRepository.save(serviceInstance);
        } else {
            throw new ServiceBrokerException(String.format("Service plan with id %s does not exist", updateServiceInstanceRequest.getPlanId()));
        }

        return serviceInstance;
    }
}
