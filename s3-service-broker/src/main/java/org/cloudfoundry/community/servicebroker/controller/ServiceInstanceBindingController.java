package org.cloudfoundry.community.servicebroker.controller;


import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ErrorMessage;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBindingResponse;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * See: Source: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 *
 * @author sgreenberg@gopivotal.com
 */
@RestController
public class ServiceInstanceBindingController extends BaseController {

    public static final String BASE_PATH = "/v2/service_instances/{instanceId}/service_bindings";

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingController.class);

    private ServiceInstanceBindingService serviceInstanceBindingService;
    private ServiceInstanceService serviceInstanceService;

    @Autowired
    public ServiceInstanceBindingController(ServiceInstanceBindingService serviceInstanceBindingService,
                                            ServiceInstanceService serviceInstanceService) {
        this.serviceInstanceBindingService = serviceInstanceBindingService;
        this.serviceInstanceService = serviceInstanceService;
    }

    @RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.PUT)
    public ResponseEntity<ServiceInstanceBindingResponse> bindServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("bindingId") String bindingId,
            @Valid @RequestBody CreateServiceInstanceBindingRequest request) throws
            ServiceInstanceDoesNotExistException, ServiceInstanceBindingExistsException,
            ServiceBrokerException {
        logger.debug("PUT: " + BASE_PATH + "/{bindingId}"
                + ", bindServiceInstance(), serviceInstance.id = " + instanceId
                + ", bindingId = " + bindingId);
        ServiceInstance instance = serviceInstanceService.getServiceInstance(instanceId);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
        ServiceInstanceBinding binding = serviceInstanceBindingService.createServiceInstanceBinding(
                request.withServiceInstanceId(instanceId).and().withBindingId(bindingId));
        logger.debug("ServiceInstanceBinding Created: " + binding.getId());
        return new ResponseEntity<ServiceInstanceBindingResponse>(
                new ServiceInstanceBindingResponse(binding),
                HttpStatus.CREATED);
    }

    @RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteServiceInstanceBinding(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("bindingId") String bindingId,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ServiceBrokerException, ServiceInstanceDoesNotExistException {
        logger.debug("DELETE: " + BASE_PATH + "/{bindingId}"
                + ", deleteServiceInstanceBinding(),  serviceInstance.id = " + instanceId
                + ", bindingId = " + bindingId
                + ", serviceId = " + serviceId
                + ", planId = " + planId);
        ServiceInstance instance = serviceInstanceService.getServiceInstance(instanceId);
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
        ServiceInstanceBinding binding = serviceInstanceBindingService.deleteServiceInstanceBinding(
                new DeleteServiceInstanceBindingRequest(bindingId, instance, serviceId, planId));
        if (binding == null) {
            return new ResponseEntity<Map>(new HashMap<>(), HttpStatus.GONE);
        }
        logger.debug("ServiceInstanceBinding Deleted: " + binding.getId());
        return new ResponseEntity<Map>(new HashMap<>(), HttpStatus.OK);
    }

    @ExceptionHandler(ServiceInstanceDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceDoesNotExistException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ServiceInstanceBindingExistsException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceBindingExistsException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
