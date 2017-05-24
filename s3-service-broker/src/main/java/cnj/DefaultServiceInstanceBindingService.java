package cnj;

import org.springframework.beans.factory.annotation.Autowired;

//@formatter:off
import org.springframework.cloud.servicebroker.exception.
        ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.
        ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model
        .CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model
        .CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model
        .CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model
        .DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service
        .ServiceInstanceBindingService;
//@formatter:on

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class DefaultServiceInstanceBindingService implements
 ServiceInstanceBindingService {

 private final ServiceInstanceBindingRepository bindingRepository;

 private final ServiceInstanceRepository instanceRepository;

 @Autowired
 DefaultServiceInstanceBindingService(ServiceInstanceBindingRepository sibr,
  ServiceInstanceRepository sir) {
  this.bindingRepository = sibr;
  this.instanceRepository = sir;
 }

 @Override
 public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
  CreateServiceInstanceBindingRequest request) {
  String bindingId = request.getBindingId();
  {
   ServiceInstanceBinding binding;
   if ((binding = this.bindingRepository.findOne(bindingId)) != null) {
    throw new ServiceInstanceBindingExistsException(
     binding.getServiceInstanceId(), binding.getId());
   }
  }

  // <1>
  ServiceInstance serviceInstance = this.instanceRepository.findOne(request
   .getServiceInstanceId());

  String username = serviceInstance.getUsername();
  String secretAccessKey = serviceInstance.getSecretAccessKey();
  String accessKeyId = serviceInstance.getAccessKeyId();

  // <2>
  Map<String, Object> credentials = new HashMap<>();
  credentials.put("bucket", username);
  credentials.put("accessKeyId", accessKeyId);
  credentials.put("accessKeySecret", secretAccessKey);

  Map<String, Object> resource = request.getBindResource();
  String appGuid = String.class.cast(resource.getOrDefault("app_guid",
   request.getAppGuid()));

  // <3>
  ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
   request.getServiceInstanceId(), null, appGuid);

  this.bindingRepository.save(binding);

  // <4>
  return new CreateServiceInstanceAppBindingResponse()
   .withCredentials(credentials);
 }

 @Override
 public void deleteServiceInstanceBinding(
  DeleteServiceInstanceBindingRequest request) {
  String bindingId = request.getBindingId();
  if (this.bindingRepository.findOne(bindingId) == null) {
   throw new ServiceInstanceBindingDoesNotExistException(bindingId);
  }
  this.bindingRepository.delete(bindingId);
 }
}
