package com.tapBattle.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfoplus;

public class GoogleOAuth {

	/**
	   * Be sure to specify the name of your application. If the application name is {@code null} or
	   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
	   */
	  private static final String APPLICATION_NAME = "Tap Battle";
	  
	  /**
	   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
	   * globally shared instance across your application.
	   */
	  private static MemoryDataStoreFactory dataStoreFactory;

	  /** Global instance of the HTTP transport. */
	  private static HttpTransport httpTransport;

	  /** Global instance of the JSON factory. */
	  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	  /** OAuth 2.0 scopes. */
	  private static final List<String> SCOPES = Arrays.asList(
	      "openid",
	      "https://www.googleapis.com/auth/userinfo.email",
	      "https://www.googleapis.com/auth/userinfo.profile");

	  private static Oauth2 oauth2;

	  /** Authorizes the installed application to access user's protected data. */
	  private static HttpRequestInitializer authorize() throws Exception {
	    // load client secrets

		  String clientId = System.getenv("CLIENT_ID");
		  String clientSecret = System.getenv("CLIENT_SECRET");

	    // set up authorization code flow
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        httpTransport, JSON_FACTORY, clientId,clientSecret, SCOPES).setDataStoreFactory(
	        dataStoreFactory).build();
	    // authorize
	    
	    return flow.getRequestInitializer();
	  }

	  public static void initialize() {
		  try {
		      //httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		      httpTransport = new NetHttpTransport.Builder()
		      	.trustCertificates(GoogleUtils.getCertificateTrustStore())
		      	//.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.etn.com", 8080)))
		      	.build();
		      
		      
		      dataStoreFactory = new MemoryDataStoreFactory();
		      // authorization
		      HttpRequestInitializer credential = authorize();

		      // set up global Oauth2 instance
		      oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
		          APPLICATION_NAME).build();
		      return;
		    } catch (IOException e) {
		      System.err.println(e.getMessage());
		    } catch (Throwable t) {
		      t.printStackTrace();
		    }
	  }

	  public static Tokeninfo userInfo(String accessToken) throws IOException {
		header("Validating a token");
	    Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
	    return tokeninfo;
	  }

	  static void header(String name) {
	    System.out.println();
	    System.out.println("================== " + name + " ==================");
	    System.out.println();
	  }
}
