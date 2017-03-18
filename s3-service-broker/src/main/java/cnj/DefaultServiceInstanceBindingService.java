package cnj;

import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultServiceInstanceBindingService implements
 ServiceInstanceBindingService {

 private final ServiceInstanceBindingRepository bindingRepository;
 private final ServiceInstanceRepository instanceRepository;

 public DefaultServiceInstanceBindingService(
  ServiceInstanceBindingRepository sibr, ServiceInstanceRepository sir) {
  this.bindingRepository = sibr;
  this.instanceRepository = sir;
 }

 @Override
 public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
  CreateServiceInstanceBindingRequest request) {

  this.assertBindingDoesNotExist(request);

  ServiceInstance serviceInstance = this.instanceRepository
   .findOne(request.getServiceInstanceId());

  String username = serviceInstance.getUsername(), secretAccessKey = serviceInstance
   .getSecretAccessKey(), accessKeyId = serviceInstance.getAccessKeyId();

  Map<String, Object> credentials = new HashMap<>();
  credentials.put("bucket", username);
  credentials.put("accessKeyId", accessKeyId);
  credentials.put("accessKeySecret", secretAccessKey);

  ServiceInstanceBinding binding = new ServiceInstanceBinding(
   request.getBindingId(), request.getServiceInstanceId(), null,
   String.class.cast(request.getBindResource().getOrDefault("app_guid",
    request.getAppGuid())));

  this.bindingRepository.save(binding);

  return new CreateServiceInstanceAppBindingResponse()
   .withCredentials(credentials);
 }

 private void assertBindingDoesNotExist(
  CreateServiceInstanceBindingRequest request) {
  ServiceInstanceBinding binding;
  if ((binding = this.bindingRepository.findOne(request
   .getBindingId())) != null) {
   throw new ServiceInstanceBindingExistsException(
    binding.getServiceInstanceId(), binding.getId());
  }
 }

 @Override
 public void deleteServiceInstanceBinding(
  DeleteServiceInstanceBindingRequest request) {
  String bindingId = request.getBindingId();
  ServiceInstanceBinding binding = this.bindingRepository
   .findOne(bindingId);
  if (binding == null) {
   throw new ServiceInstanceBindingDoesNotExistException(bindingId);
  }
  this.bindingRepository.delete(bindingId);
 }

}
