package com.my.quiztaker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Attempt {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private Long attemptId;
	
	@Column(nullable = true, updatable = true)
	private Integer score;
	
	@Column(nullable = false)
	private int rating;

	@ManyToOne
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public Attempt() {}

	public Attempt(int score, Quiz quiz, User user, int rating) {
		this.score = score;
		this.quiz = quiz;
		this.user = user;
		this.rating = rating;
	}

	public Attempt(Quiz quiz, User user) {
		this.score = null;
		this.quiz = quiz;
		this.user = user;
		this.rating = 5;
	}

	public Long getAttemptId() {
		return attemptId;
	}

	public void setAttemptId(Long attemptId) {
		this.attemptId = attemptId;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
