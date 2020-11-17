package com.tapBattle.server.entities;

public enum BattleStatus {

	CREATED("Created"),
	SEARCHING("Searching"),
	STARTING("Starting"),
	COUNTDOWN("Countdown"),
	BATTLE("Battle"),
	COMPLETE("Complete");
	
	private String status;
	
	private BattleStatus(String status) {
		this.status = status;
	}
}
