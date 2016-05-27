package org.cloudfoundry.community.servicebroker.service;

import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.*;

/**
 * Handles bindings to service instances.
 *
 * @author sgreenberg@gopivotal.com
 */
public interface ServiceInstanceBindingService {

    /**
     * Creates a new binding to a service instance
     *
     * @param createServiceInstanceBindingRequest containing parameters sent from Cloud Controller
     * @return the new ServiceInstanceBinding for the request
     */
    ServiceInstanceBinding createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException;

    /**
     * Deletes an existing binding to a service instance
     *
     * @param deleteServiceInstanceBindingRequest sent from the Cloud Controller
     * @return The deleted ServiceInstanceBinding, or returns null if no binding exists
     * @throws ServiceBrokerException if a failure occurred during deletion of a binding
     */
    ServiceInstanceBinding deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest)
            throws ServiceBrokerException;
}
