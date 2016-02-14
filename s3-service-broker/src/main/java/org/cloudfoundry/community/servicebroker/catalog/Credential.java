package org.cloudfoundry.community.servicebroker.catalog;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Credential {
    @Id
    private String id;

    private String userName;
    private String accessKeyId;
    private String secretAccessKey;

    public Credential() {
        id = UUID.randomUUID().toString();
    }

    public Credential(String userName, String accessKeyId, String secretAccessKey) {
        this();
        this.userName = userName;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", secretAccessKey='" + secretAccessKey + '\'' +
                '}';
    }
}
