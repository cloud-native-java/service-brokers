package cnj;

import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;


@Configuration
class CatalogConfiguration {

	@Bean
	public Catalog catalog() {

		List<ServiceDefinition> definitions = Collections.singletonList(
				new ServiceDefinition(
						"0941881744418",
						"s3-service-broker",
						"A simple AWS S3 service broker implementation",
						true,
						false,
						Collections.singletonList(
								new Plan("841794608635",
										"basic",
										"Amazon S3 bucket with unlimited storage",
										getPlanMetadata(), true, true)),
						Arrays.asList("s3", "storage", "cache", "AWS"),
						getServiceDefinitionMetadata(),
						null,
						null));

		return new Catalog(definitions);
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<>();
		sdMetadata.put("displayName", "s3-service-broker");
		sdMetadata.put("providerDisplayName", "s3-service-broker");
		sdMetadata.put("longDescription", "A backing service with unlimited Amazon S3 storage");
		sdMetadata.put("documentationUrl", "http://aws.amazon.com/s3");
		sdMetadata.put("supportUrl", "http://aws.amazon.com/s3");
		return sdMetadata;
	}

	private Map<String, Object> getPlanMetadata() {
		Map<String, Object> planMetadata = new HashMap<>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		return planMetadata;
	}

	private Map<String, Object> getCosts() {
		return Collections.singletonMap("free", true);
	}

	private List<String> getBullets() {
		return Collections.emptyList();
	}

}
