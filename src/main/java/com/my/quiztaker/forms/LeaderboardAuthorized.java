package com.my.quiztaker.forms;

import java.util.List;

public class LeaderboardAuthorized {
	private List<UserPublic> users;
	private Integer position;
	
	public LeaderboardAuthorized(List<UserPublic> users, Integer position) {
		this.users = users;
		this.position = position;
	}

	public List<UserPublic> getUsers() {
		return users;
	}

	public void setUsers(List<UserPublic> users) {
		this.users = users;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}
}
