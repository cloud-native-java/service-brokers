package org.cloudfoundry.community.servicebroker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.community.servicebroker.catalog.Plan;
import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.repositories.ServiceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Arrays;
import java.util.Collections;


/**
 * Amazon S3 storage broker for attaching simple storage to Cloud Foundry applications
 *
 * @author kbastani
 */
@SpringBootApplication
public class ServiceBrokerApplication {

    @Autowired
    private ServiceDefinitionRepository serviceDefinitionRepository;

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceBrokerApplication.class).web(true).run(args);
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.json()
                .defaultViewInclusion(false)
                .autoDetectFields(true)
                .indentOutput(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return strings -> {

            // Initialize the service broker definition when running in cloud profile
            if (Arrays.asList(environment.getActiveProfiles()).contains("cloud")) {

                // Initialize a default service definition for the purpose of this example
                Plan plan = new Plan("ac8fdb55-3223-41e9-a5f5-eca6f8fd40c0", "s3-basic",
                        "Amazon S3 bucket with unlimited storage");

                // Set plan to free
                plan.setFree(true);

                // Create the default service definition describing the broker's purpose
                ServiceDefinition serviceDefinition =
                        new ServiceDefinition("00a3b868-9cf9-4ad3-a6b0-e867740cbef0", "amazon-s3",
                                "Amazon S3 simple storage as a backing service", true, Collections.singleton(plan));

                serviceDefinitionRepository.save(serviceDefinition);
            }
        };
    }
}