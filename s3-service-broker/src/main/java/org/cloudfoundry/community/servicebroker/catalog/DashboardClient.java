package org.cloudfoundry.community.servicebroker.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class DashboardClient implements Serializable {

    @Id
	private String id;
	private String secret;

	@JsonProperty("redirect_uri")
	private String redirectUri;

	public DashboardClient() {
	}

	public DashboardClient(String id, String secret, String redirectUri) {
		this.id = id;
		this.secret = secret;
		this.redirectUri = redirectUri;
	}

	public String getId() {
		return id;
	}

	public String getSecret() {
		return secret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}
	
}
