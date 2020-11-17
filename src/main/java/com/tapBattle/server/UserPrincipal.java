package com.tapBattle.server;

import java.security.Principal;

import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfoplus;

public class UserPrincipal implements Principal{

	private final Tokeninfo userInfo;
	
	UserPrincipal(Tokeninfo userInfo){
		this.userInfo = userInfo;
	}
	
	@Override
	public String getName() {
		return this.userInfo.getEmail();
	}
	
	public String getEmail() {
		return this.userInfo.getEmail();
	}
	
	public String getId() {
		return this.userInfo.getUserId();
	}

}
