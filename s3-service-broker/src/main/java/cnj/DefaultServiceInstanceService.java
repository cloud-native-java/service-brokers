package cnj;

import cnj.s3.S3Service;
import cnj.s3.S3User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

@Service
class DefaultServiceInstanceService implements ServiceInstanceService {

 private final S3Service s3Service;

 private final ServiceInstanceRepository instanceRepository;

 private Log log = LogFactory.getLog(getClass());

 @Autowired
 DefaultServiceInstanceService(S3Service s3Service,
  ServiceInstanceRepository instanceRepository) {
  this.s3Service = s3Service;
  this.instanceRepository = instanceRepository;
 }

 @Override
 public CreateServiceInstanceResponse createServiceInstance(
  CreateServiceInstanceRequest request) {
  if (!this.exists(request.getServiceInstanceId())) {
   ServiceInstance si = new ServiceInstance(request);
   S3User user = s3Service.createS3UserAndBucket(si.getId()); // <1>
   si.setSecretAccessKey(user.getAccessKeySecret());
   si.setUsername(user.getUsername());
   si.setAccessKeyId(user.getAccessKeyId());
   this.instanceRepository.save(si);
  }
  else {
   this.error("could not create serviceInstance "
    + request.getServiceInstanceId());
  }
  return new CreateServiceInstanceResponse();
 }

 @Override
 public DeleteServiceInstanceResponse deleteServiceInstance(
  DeleteServiceInstanceRequest request) {
  String sid = request.getServiceInstanceId();
  if (this.exists(sid)) {
   ServiceInstance si = this.instanceRepository.findOne(sid);
   // <2>
   boolean deleteSucceeded = this.s3Service.deleteBucket(si.getId(),
    si.getAccessKeyId(), si.getUsername());
   if (!deleteSucceeded) {
    log.error("could not delete the S3 bucket for service instance " + sid);
   }
   this.instanceRepository.delete(si.getId());
  }
  else {
   this.error("could not delete the S3 service instance " + sid);
  }
  return new DeleteServiceInstanceResponse();
 }

 // <3>
 @Override
 public UpdateServiceInstanceResponse updateServiceInstance(
  UpdateServiceInstanceRequest request) {
  String sid = request.getServiceInstanceId();
  if (this.exists(sid)) {
   ServiceInstance instance = this.instanceRepository.findOne(sid);
   this.instanceRepository.delete(instance);
   this.instanceRepository.save(new ServiceInstance(request));
  }
  else {
   this.error("could not update serviceInstance " + sid);
  }
  return new UpdateServiceInstanceResponse();
 }

 @Override
 public GetLastServiceOperationResponse getLastOperation(
  GetLastServiceOperationRequest request) {
  return new GetLastServiceOperationResponse()
   .withOperationState(OperationState.SUCCEEDED);
 }

 private void error(String msg) {
  throw new ServiceBrokerException(msg);
 }

 private boolean exists(String serviceInstanceId) {
  return instanceRepository.exists(serviceInstanceId);
 }
}