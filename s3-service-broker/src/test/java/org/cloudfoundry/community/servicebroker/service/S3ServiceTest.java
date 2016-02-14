package org.cloudfoundry.community.servicebroker.service;

import org.cloudfoundry.community.servicebroker.ServiceBrokerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ServiceBrokerApplication.class)
@WebIntegrationTest
@ActiveProfiles("test")
public class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    @Test
    public void testCreateContainer() throws Exception {
        s3Service.createBucket(UUID.randomUUID().toString());
    }
}