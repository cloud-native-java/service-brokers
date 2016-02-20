package org.cloudfoundry.community.servicebroker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


/**
 * Amazon S3 storage broker for attaching simple storage to Cloud Foundry applications
 *
 * @author kbastani
 */
@SpringBootApplication
public class ServiceBrokerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceBrokerApplication.class).run(args);
    }
}