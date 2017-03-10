package cnj;

import cnj.s3.S3Service;
import cnj.s3.S3User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

@Service
public class DefaultServiceInstanceService implements ServiceInstanceService {

	private final S3Service s3Service;
	private final ServiceInstanceRepository serviceInstanceRepository;

	private Log log = LogFactory.getLog(getClass());

	public DefaultServiceInstanceService(
			S3Service s3Service, ServiceInstanceRepository serviceInstanceRepository) {
		this.s3Service = s3Service;
		this.serviceInstanceRepository = serviceInstanceRepository;
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		if (!this.exists(request.getServiceInstanceId())) {
			ServiceInstance si = new ServiceInstance(request);
			S3User user = s3Service.createBucket(si.getId());
			si.setSecretAccessKey(user.getAccessKeySecret());
			si.setUsername(user.getCreateUserResult().getUser().getUserName());
			si.setAccessKeyId(user.getAccessKeyId());
			this.serviceInstanceRepository.save(si);
		}
		else {
			this.error("could not create serviceInstance " + request.getServiceInstanceId());
		}
		return new CreateServiceInstanceResponse();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		return new GetLastServiceOperationResponse()
				.withOperationState(OperationState.SUCCEEDED);
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		if (this.exists(request.getServiceInstanceId())) {
			ServiceInstance si = this.serviceInstanceRepository.findOne(request.getServiceInstanceId());
			if (!this.s3Service.deleteServiceInstanceBucket(
					si.getId(), si.getAccessKeyId(), si.getUsername())) {
				log.error("could not delete the S3 bucket for service instance " + request.getServiceInstanceId());
			}
			this.serviceInstanceRepository.delete(si.getId());
		}
		else {
			this.error("could not delete the S3 service instance " + request.getServiceInstanceId());
		}
		return new DeleteServiceInstanceResponse();
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		String sid = request.getServiceInstanceId();
		if (this.exists(sid)) {
			ServiceInstance instance = this.serviceInstanceRepository.findOne(sid);
			this.serviceInstanceRepository.delete(instance);
			ServiceInstance serviceInstance = new ServiceInstance(request);
			this.serviceInstanceRepository.save(serviceInstance);
		}
		else {
			this.error("could not update serviceInstance " + request.getServiceInstanceId());
		}
		return new UpdateServiceInstanceResponse();
	}

	private void error(String msg) {
		throw new ServiceBrokerException(msg);
	}

	private boolean exists(String si) {
		return serviceInstanceRepository.exists(si);
	}
}