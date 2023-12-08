package com.my.quiztaker.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Difficulty {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private Long difficultyId;
	
	@Column
	private String name;
	
	@Column(nullable = false)
	private Double rate;
	
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "difficulty")
	private List<Quiz> quizzes;
	
	public Difficulty() {}
	
	public Difficulty(String name, Double rate) {
		this.name = name;
		this.rate = rate;
	}

	public Long getDifficultyId() {
		return difficultyId;
	}

	public void setDifficultyId(Long difficultyId) {
		this.difficultyId = difficultyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public List<Quiz> getQuizzes() {
		return quizzes;
	}

	public void setQuizzes(List<Quiz> quizzes) {
		this.quizzes = quizzes;
	}
}
