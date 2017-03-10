package cnj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class DefaultServiceInstanceBindingService implements ServiceInstanceBindingService {

	private final ServiceInstanceBindingRepository serviceInstanceBindingRepository;
	private final ServiceInstanceRepository serviceInstanceRepository;
	private final Log log = LogFactory.getLog(getClass());

	public DefaultServiceInstanceBindingService(ServiceInstanceBindingRepository sibr,
	                                            ServiceInstanceRepository sir) {
		this.serviceInstanceBindingRepository = sibr;
		this.serviceInstanceRepository = sir;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {

		this.assertBindingDoesNotExist(request);

		ServiceInstance serviceInstance = this.serviceInstanceRepository.findOne(request.getServiceInstanceId());

		String username = serviceInstance.getUsername(),
				secretAccessKey = serviceInstance.getSecretAccessKey(),
				accessKeyId = serviceInstance.getAccessKeyId();

		Map<String, Object> credentials = new HashMap<>();
		credentials.put("userName", username);
		credentials.put("accessKeyId", accessKeyId);
		credentials.put("secretAccessKey", secretAccessKey);

		ServiceInstanceBinding binding = new ServiceInstanceBinding(
				request.getBindingId(),
				request.getServiceInstanceId(),
				null,
				String.class.cast(request.getBindResource().getOrDefault("app_guid", request.getAppGuid())));

		this.serviceInstanceBindingRepository.save(binding);

		return new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);
	}

	private void assertBindingDoesNotExist(CreateServiceInstanceBindingRequest request) {
		ServiceInstanceBinding binding;
		if ((binding = this.serviceInstanceBindingRepository.findOne(request.getBindingId())) != null) {
			throw new ServiceInstanceBindingExistsException(
					binding.getServiceInstanceId(), binding.getId());
		}
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = this.serviceInstanceBindingRepository.findOne(bindingId);
		assertServiceInstanceDoesExist(bindingId, binding);
		this.serviceInstanceBindingRepository.delete(bindingId);
	}

	private void assertServiceInstanceDoesExist(String bindingId, ServiceInstanceBinding binding) {
		if (binding == null) {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}
}


