package com.my.quiztaker.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@Entity
public class Quiz {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private Long quizId;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = true)
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "difficulty_id")
	private Difficulty difficulty;
	
	@Column(nullable = false)
	private int minutes;
	
	@Column(nullable = false)
	private String status;
	
	@JsonIncludeProperties({"id"})
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz")
	private List<Attempt> attempts;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz")
	private List<Question> questions;
	
	public Quiz() {}
	
	public Quiz(User user, Category category, Difficulty difficulty) {
		this.title = "Quiz Title";
		this.description = "Quiz Description";
		this.difficulty = difficulty;
		this.category = category;
		this.user = user;
		this.minutes = 5;
		this.status = "Created";
	}

	public Quiz(String title, String description, Category category, Difficulty difficulty, User user, int minutes, String status) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.difficulty = difficulty;
		this.user = user;
		this.minutes = minutes;
		this.status = status;
	}

	public Quiz(String title, Category category, Difficulty difficulty, User user, int minutes, String status) {
		this.description = null;
		this.title = title;
		this.category = category;
		this.difficulty = difficulty;
		this.user = user;
		this.minutes = minutes;
		this.status = status;
	}

	public Long getQuizId() {
		return quizId;
	}

	public void setQuizId(Long quizId) {
		this.quizId = quizId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public List<Attempt> getAttempts() {
		return attempts;
	}

	public void setAttempts(List<Attempt> attempts) {
		this.attempts = attempts;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
