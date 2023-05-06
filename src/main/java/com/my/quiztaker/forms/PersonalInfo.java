package com.my.quiztaker.forms;

public class PersonalInfo {
	private String username;
	private String email;
	private Double score;
	private Integer attempts;
	private Integer position;
	
	public PersonalInfo(String username, String email, Double score, Integer attempts, Integer position) {
		this.username = username;
		this.email = email;
		this.score = score;
		this.attempts = attempts;
		this.position = position;
	}
	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
}
