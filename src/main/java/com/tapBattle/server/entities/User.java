package com.tapBattle.server.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.atmosphere.cpr.AtmosphereResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jersey.repackaged.com.google.common.base.MoreObjects;
import jersey.repackaged.com.google.common.base.Objects;

@Entity
@Table(name = "Tap_User")
public class User{

	@Id
	@Column(name = "User_id")
	private String id;
	
	private String username;
	
	private Long taps;
	
	private Integer totalSecondsPlayed;
	
	public User() {
		setTaps(new Long(0));
		setTotalSecondsPlayed(0);
		
	}
	
	public User(String id) {
		this.id = id;
		setTaps(new Long(0));
		setTotalSecondsPlayed(0);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getTaps() {
		if(taps == null) {
			taps = new Long(0);
		}
		return taps;
	}

	public void setTaps(Long taps) {
		this.taps = taps;
	}
	
	public Integer getTotalSecondsPlayed() {
		if(totalSecondsPlayed == null) {
			totalSecondsPlayed = 0;
		}
		return totalSecondsPlayed;
	}

	public void setTotalSecondsPlayed(Integer totalSecondsPlayed) {
		this.totalSecondsPlayed = totalSecondsPlayed;
	}
	

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof User)) return false;
		return id != null && id.equals(((User)o).getId());
	}
	
	
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", this.getId())
				.add("username", this.getUsername())
				.add("taps", this.getTaps())
				.add("Seconds Played", this.getTotalSecondsPlayed()).toString();
				
	}
}
