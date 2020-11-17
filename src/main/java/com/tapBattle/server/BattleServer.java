package com.tapBattle.server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.atmosphere.cpr.AtmosphereResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.tapBattle.server.entities.Battle;
import com.tapBattle.server.entities.BattleStatus;
import com.tapBattle.server.entities.User;

public class BattleServer {
	
	private static ConcurrentHashMap<String, User> connectedResources;
	private static ConcurrentHashMap<String, AtmosphereResource> connectedUsers;
	
	private static BattleServer server;
	
	private static ConcurrentHashMap<String, Battle> currentBattles;
	
	public static ObjectMapper mapper = new ObjectMapper();
	
	private static Timer timer;
	
	private static EntityManager manager;

	public static final Integer BEAT_BY_TAPS = 40;
	
	public BattleServer() {
		this.timer = new Timer();
		connectedUsers = new ConcurrentHashMap<>();
		connectedResources = new ConcurrentHashMap<>();
		currentBattles = new ConcurrentHashMap<>();
	}
	
	public static void initialize(EntityManager manager) {
		if(server == null) {
			server = new BattleServer();
			BattleServer.manager = manager;
		}
	}
	
	public static BattleServer getBattleServer() {
		return BattleServer.server;
	}
	
	public static void registerUser(User user, AtmosphereResource resource) {
		connectedResources.put(resource.uuid(), user);
		connectedUsers.put(user.getId(), resource);
	}
	
	public static void disconnectResource(AtmosphereResource resource) {
		User user = connectedResources.remove(resource.uuid());
		connectedUsers.remove(user.getId());
		Optional<Battle> openBattle = currentBattles.entrySet().stream().map(entry -> entry.getValue()).filter(battle -> {
			if(user.equals(battle.getPlayer1()) || user.equals(battle.getPlayer2())) {
				return true;
			}
			return false;
		}).findAny();
		openBattle.ifPresent(battle -> {
			if(user.equals(battle.getPlayer1())) {
				battle.setPlayer1(null);
				battle.setBattleStatus(BattleStatus.SEARCHING);
			}else {
				battle.setPlayer2(null);
				battle.setBattleStatus(BattleStatus.SEARCHING);
			}
			if(battle.getPlayer1() == null && battle.getPlayer2() == null) {
				currentBattles.remove(battle.getId());
				System.out.println("BattleServer: Battle canceled after user left");
			}
			battle.updateUsers();
		});
	}
	
	public static AtmosphereResource getResourceForUser(User user) {
		return connectedUsers.get(user.getId());
	}
	
	public static void startBattle(User user) {
		Optional<Battle> openBattle = currentBattles.entrySet().stream().map(entry -> entry.getValue())
			.filter(battle -> battle.getBattleStatus().equals(BattleStatus.SEARCHING)) //battles searching
			.filter(battle -> !(battle.getPlayer1() == null ? battle.getPlayer2() : battle.getPlayer1())
					.getId().equals(user.getId())) //dont join yourself in a battle
			.findFirst();
		if(openBattle.isPresent()) {
			Battle battle = openBattle.get();
			battle.setBattleStatus(BattleStatus.STARTING);
			if(battle.getPlayer1() != null) {
				battle.setPlayer2(user);
			}else {
				battle.setPlayer1(user);
			}
			initializeBattle(battle);
		}else {
			Battle battle = new Battle();
			battle.setBattleStatus(BattleStatus.SEARCHING);
			battle.setPlayer1(user);
			currentBattles.put(battle.getId(), battle);
			battle.updateUsers();
		}
	}
	
	public static void addUserTapToBattle(User user, String battleId) {
		Optional.ofNullable(currentBattles.get(battleId))
			.ifPresent(battle -> {
				if(battle.getBattleStatus() == BattleStatus.BATTLE) {
					if(battle.getPlayer1().equals(user)) {
						battle.setPlayer1Taps(battle.getPlayer1Taps() + 1);
					}else {
						battle.setPlayer2Taps(battle.getPlayer2Taps() + 1);
					}

					//close the battle if the user is ahead by the set amount
					if(Math.abs(battle.getPlayer1Taps() - battle.getPlayer2Taps()) >= BattleServer.BEAT_BY_TAPS){
						battle.setTimer(0);
						battle.setBattleStatus(BattleStatus.COMPLETE);
						battle.setWinner((battle.getPlayer1Taps() > battle.getPlayer2Taps()) ? battle.getPlayer1() : battle.getPlayer2());
						battle.updateUsers();
						closeOutBattle(battle);
					}else{ //otherwise, update all players
						battle.updateUsers();
					}
				}
			});
	}
	
	private static void initializeBattle(Battle battle) {
		battle.setTimer(2);
		battle.updateUsers();
		runAfter(() -> countdownBattle(battle), 1000);
	}
	
	/**
	 * This method is responsible for counting down the timer for the given battle. A battle 
	 * is ticking down time till when it starts, till when the battle starts, and then to when the 
	 * battle is done. This simply steps the timer down if the battle is in one of those states. If 
	 * the timer hits zero, it will trigger a method to update the BattleState of the battle.
	 * @param battle the battle to tick down
	 */
	private static void countdownBattle(Battle battle) {
		if(battle.getBattleStatus() == BattleStatus.STARTING ||
		   battle.getBattleStatus() == BattleStatus.COUNTDOWN || 
		   battle.getBattleStatus() == BattleStatus.BATTLE) {
			if(battle.getTimer() > 0) {
				battle.setTimer(battle.getTimer() - 1);
			}
			if(battle.getTimer() == 0) {
				updateBattleState(battle);
				battle.updateUsers();
			}else {
				battle.updateUsers();
				runAfter(() -> countdownBattle(battle), 1000);
			}
		}
	}
	
	/**
	 * After the battle timer ticks down to zero, this method is called to advance the state of the battle. 
	 * The battles state moves from:
	 * STARTING
	 * COUNTDOWN
	 * BATTLE
	 * COMPLETE
	 * @param battle the battle to update the state on
	 */
	private static void updateBattleState(Battle battle) {
		if(battle.getTimer() == 0) {
			switch(battle.getBattleStatus()) {
			case STARTING:
				battle.setTimer(5);
				battle.setBattleStatus(BattleStatus.COUNTDOWN);
				runAfter(() -> countdownBattle(battle), 1000);
				break;
			case COUNTDOWN:
				battle.setTotalTime((new Random()).nextInt(15) + 5);
				battle.setTimer(battle.getTotalTime());
				battle.setBattleStatus(BattleStatus.BATTLE);
				runAfter(() -> countdownBattle(battle), 1000);
				break;
			case BATTLE:
				battle.setTimer(0);
				battle.setBattleStatus(BattleStatus.COMPLETE);
				battle.setWinner((battle.getPlayer1Taps() > battle.getPlayer2Taps()) ? battle.getPlayer1() : battle.getPlayer2());
				battle.updateUsers();
				closeOutBattle(battle);
				break;
			}
			battle.updateUsers();
		}
	}
	
	private static void closeOutBattle(Battle battle) {
		while(manager.getTransaction().isActive()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		manager.getTransaction().begin();

		battle.setTimestamp(new Date());
		
		
		
		TypedQuery<User> query = manager.createQuery("from User as u where u.id = :id1 or u.id = :id2", User.class);
		query.setParameter("id1", (battle.getPlayer1() != null) ? battle.getPlayer1().getId() : "");
		query.setParameter("id2", (battle.getPlayer2() != null) ? battle.getPlayer2().getId() : "");
		try {
			List<User> users = query.getResultList();
			User player1 = null;
			User player2 = null;
			if(battle.getPlayer1() != null) {
				Optional<User> player1Opt = users.stream().filter(user -> user.getId().equals(battle.getPlayer1().getId()))
					.findFirst();
				if(player1Opt.isPresent()) {
					player1 = player1Opt.get();
					battle.setPlayer1(player1);
				}else {
					battle.setPlayer1(null);
				}
			}
			if(battle.getPlayer2() != null) {
				Optional<User> player2Opt = users.stream().filter(user -> user.getId().equals(battle.getPlayer2().getId()))
					.findFirst();
				if(player2Opt.isPresent()) {
					player2 = player2Opt.get();
					battle.setPlayer2(player2);
				}else {
					battle.setPlayer2(null);
				}
			}
			manager.merge(battle);
			
			if(player1 != null) {
				player1.setTaps(player1.getTaps() + battle.getPlayer1Taps());
				player1.setTotalSecondsPlayed(player1.getTotalSecondsPlayed() + battle.getTotalTime());
				manager.merge(player1);
			}
			if(player2 != null) {
				player2.setTaps(player2.getTaps() + battle.getPlayer2Taps());
				player2.setTotalSecondsPlayed(player2.getTotalSecondsPlayed() + battle.getTotalTime());
				manager.merge(player2);
			}
		}catch(javax.persistence.NoResultException e) {
			
		}
		manager.getTransaction().commit();
	}
	
	/**
	 * This method allows you to pass a lambda runnable function to it, and have the Java Timer run the 
	 * lambda function after a provided millisecond delay.
	 * @param run a lambda runnable function to run after a delay
	 * @param milis an integer value of delay, in milliseconds
	 */
	private static void runAfter(Runnable run, int milis) {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				run.run();
			}
		}, milis);
	}
	
	
}
