package org.cloudfoundry.community.servicebroker.model;

import com.amazonaws.services.identitymanagement.model.CreateUserResult;

public class S3User {

    private String applicationId;
    private CreateUserResult createUserResult;
    private String accessKeyId;
    private String accessKeySecret;

    public S3User(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public CreateUserResult getCreateUserResult() {
        return createUserResult;
    }

    public void setCreateUserResult(CreateUserResult createUserResult) {
        this.createUserResult = createUserResult;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Override
    public String toString() {
        return "S3User{" +
                "applicationId='" + applicationId + '\'' +
                ", createUserResult=" + createUserResult +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='" + accessKeySecret + '\'' +
                '}';
    }
}
