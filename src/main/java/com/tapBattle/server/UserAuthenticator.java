package com.tapBattle.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class UserAuthenticator implements Authenticator<String, UserPrincipal>{

	@Override
	public Optional<UserPrincipal> authenticate(String accessToken) throws AuthenticationException {
		try {
			return Optional.of(new UserPrincipal(GoogleOAuth.userInfo(accessToken)));
		} catch (IOException e) {
			//MainWindow.displayErrorMessage(e);
			e.printStackTrace();
		}
		return Optional.empty();
	}

}
