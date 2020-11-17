package com.tapBattle.server.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tapBattle.server.BattleServer;

@Entity
@Table(name = "Tap_Battle")
public class Battle {

	@Id
	@Column(name = "Battle_id")
	private String id;
	
	@JsonFormat(pattern="M-dd-yy h:mma")
	private Date timestamp;
	
	@OneToOne()
	@JoinColumn(name="player1_user_id")
	@JsonIdentityReference(alwaysAsId = true)
	private User player1;
	
	@OneToOne()
	@JoinColumn(name="player2_user_id")
	@JsonIdentityReference(alwaysAsId = true)
	private User player2;
	
	private Integer player1Taps;
	
	private Integer player2Taps;
	
	@Transient
	transient private BattleStatus status;
	
	@Transient
	transient private Integer timer;

	private Integer totalTime;
	
	@OneToOne()
	@JoinColumn(name="winner_user_id")
	@JsonIdentityReference(alwaysAsId = true)
	private User winner;
	
	public Battle() {
		this.player1Taps = 0;
		this.player2Taps = 0;
		this.status = BattleStatus.CREATED;
		this.timer = 0;
		this.totalTime = 0;
		this.id = (new SimpleDateFormat("MM-dd-yyyy@HH:mm:ss")).format(new Date()) + Long.toString((new Random()).nextLong());
	}
	
	public String getId() {
		return id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Transient
	public BattleStatus getBattleStatus() {
		return status;
	}
	
	public void setBattleStatus(BattleStatus status) {
		this.status = status;
	}
	
	@Transient
	public Integer getTimer() {
		return timer;
	}

	public void setTimer(Integer timer) {
		this.timer = timer;
	}

	public Integer getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Integer totalTime) {
		this.totalTime = totalTime;
	}

	public User getPlayer1() {
		return player1;
	}

	public void setPlayer1(User player1) {
		this.player1 = player1;
	}

	public User getPlayer2() {
		return player2;
	}

	public void setPlayer2(User player2) {
		this.player2 = player2;
	}

	public Integer getPlayer1Taps() {
		return player1Taps;
	}

	public void setPlayer1Taps(Integer player1Taps) {
		this.player1Taps = player1Taps;
	}

	public Integer getPlayer2Taps() {
		return player2Taps;
	}

	public void setPlayer2Taps(Integer player2Taps) {
		this.player2Taps = player2Taps;
	}

	public User getWinner() {
		return winner;
	}

	public void setWinner(User winner) {
		this.winner = winner;
	}

	public void updateUsers() {
		try {
			if(this.getPlayer1() != null) {
				BattleServer.getResourceForUser(this.getPlayer1()).write(BattleServer.mapper.writeValueAsString(this));
			}
			if(this.getPlayer2() != null) {
				BattleServer.getResourceForUser(this.getPlayer2()).write(BattleServer.mapper.writeValueAsString(this));
			}
		} catch (JsonProcessingException e) {
			//MainWindow.displayErrorMessage(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Battle)) return false;
		return id != null && id.equals(((Battle)o).getId());
	}
	
	
}
