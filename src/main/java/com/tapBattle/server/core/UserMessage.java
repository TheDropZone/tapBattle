package com.tapBattle.server.core;

import com.tapBattle.server.entities.Battle;
import com.tapBattle.server.entities.User;

public class UserMessage {

	public User user;
	
	public Battle battle;
	
	public String message;
	
	public String messageType;
	
	public UserMessage() {
		
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public String getMessageType() {
		return messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Battle getBattle() {
		return battle;
	}

	public void setBattle(Battle battle) {
		this.battle = battle;
	}
	
	
}
