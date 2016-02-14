package org.cloudfoundry.community.servicebroker.controller;

import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstance;
import org.cloudfoundry.community.servicebroker.exception.*;
import org.cloudfoundry.community.servicebroker.model.*;
import org.cloudfoundry.community.servicebroker.service.CatalogService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * See: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 *
 * @author sgreenberg@gopivotal.com
 */
@Controller
public class ServiceInstanceController extends BaseController {

    public static final String BASE_PATH = "/v2/service_instances";

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceController.class);

    private ServiceInstanceService service;
    private CatalogService catalogService;

    @Autowired
    public ServiceInstanceController(ServiceInstanceService service, CatalogService catalogService) {
        this.service = service;
        this.catalogService = catalogService;
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}/last_operation", method = RequestMethod.GET)
    public ResponseEntity<ServiceInstanceStateResponse> getLastServiceInstanceOperation(@PathVariable("instanceId") String serviceInstanceId)
            throws ServiceDefinitionDoesNotExistException, ServiceInstanceExistsException, ServiceBrokerException, ServiceInstanceDoesNotExistException {

        logger.debug("GET: " + BASE_PATH + "/{instanceId}/last_operation, getLastServiceInstanceOperation(), serviceInstanceId = " + serviceInstanceId);
        ServiceInstance instance = service.getServiceInstance(serviceInstanceId);
        logger.debug("Get ServiceInstance State: " + instance.getServiceInstanceId());
        return new ResponseEntity<>(
                new ServiceInstanceStateResponse("succeeded", "Service created"),
                HttpStatus.OK);
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.GET)
    public ResponseEntity<ServiceInstance> getServiceInstance(@PathVariable("instanceId") String serviceInstanceId)
            throws ServiceDefinitionDoesNotExistException, ServiceInstanceExistsException, ServiceBrokerException, ServiceInstanceDoesNotExistException {

        logger.debug("GET: " + BASE_PATH + "/{instanceId}, getServiceInstance(), serviceInstanceId = " + serviceInstanceId);
        ServiceInstance instance = service.getServiceInstance(serviceInstanceId);
        logger.debug("Get ServiceInstance: " + instance.getServiceInstanceId());
        return new ResponseEntity<>(
                instance,
                HttpStatus.OK);
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.PUT)
    public ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(
            @PathVariable("instanceId") String serviceInstanceId,
            @Valid @RequestBody CreateServiceInstanceRequest request) throws
            ServiceDefinitionDoesNotExistException,
            ServiceInstanceExistsException,
            ServiceBrokerException {
        logger.debug("PUT: " + BASE_PATH + "/{instanceId}"
                + ", createServiceInstance(), serviceInstanceId = " + serviceInstanceId);
        ServiceDefinition svc = catalogService.getServiceDefinition(request.getServiceDefinitionId());
        if (svc == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }
        ServiceInstance instance = service.createServiceInstance(
                request.withServiceDefinition(svc).and().withServiceInstanceId(serviceInstanceId));
        logger.debug("ServiceInstance Created: " + instance.getServiceInstanceId());
        return new ResponseEntity<CreateServiceInstanceResponse>(
                new CreateServiceInstanceResponse(instance),
                HttpStatus.CREATED);
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @RequestParam("service_id") String serviceId,
            @RequestParam("plan_id") String planId) throws ServiceBrokerException, ServiceInstanceDoesNotExistException {
        logger.debug("DELETE: " + BASE_PATH + "/{instanceId}"
                + ", deleteServiceInstanceBinding(), serviceInstanceId = " + instanceId
                + ", serviceId = " + serviceId
                + ", planId = " + planId);
        ServiceInstance instance = service.deleteServiceInstance(
                new DeleteServiceInstanceRequest(instanceId, serviceId, planId));
        if (instance == null) {
            return new ResponseEntity<Map>(new HashMap<>(), HttpStatus.GONE);
        }
        logger.debug("ServiceInstance Deleted: " + instance.getServiceInstanceId());
        return new ResponseEntity<Map>(new HashMap<>(), HttpStatus.OK);
    }

    @RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> updateServiceInstance(
            @PathVariable("instanceId") String instanceId,
            @Valid @RequestBody UpdateServiceInstanceRequest request) throws
            ServiceInstanceUpdateNotSupportedException,
            ServiceInstanceDoesNotExistException,
            ServiceBrokerException {
        logger.debug("UPDATE: " + BASE_PATH + "/{instanceId}"
                + ", updateServiceInstanceBinding(), serviceInstanceId = "
                + instanceId + ", instanceId = " + instanceId + ", planId = "
                + request.getPlanId());
        ServiceInstance instance = service.updateServiceInstance(request.withInstanceId(instanceId));
        logger.debug("ServiceInstance updated: " + instance.getServiceInstanceId());
        return new ResponseEntity<Map>(new HashMap<>(), HttpStatus.OK);
    }


    @ExceptionHandler(ServiceDefinitionDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceDefinitionDoesNotExistException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ServiceInstanceExistsException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceExistsException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceInstanceUpdateNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceUpdateNotSupportedException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
