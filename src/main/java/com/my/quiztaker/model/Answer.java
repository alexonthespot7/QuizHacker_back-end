package com.my.quiztaker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
public class Answer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private Long answerId;
	
	@NotBlank
	@Column(nullable = false)
	private String text;
	
	@Column(nullable = false)
	private boolean isCorrect;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;
	
	public Answer() {}
	
	public Answer(Question question, boolean isCorrect) {
		this.text = "Answer";
		this.isCorrect = isCorrect;
		this.question = question;
	}

	public Answer(String text, boolean isCorrect, Question question) {
		this.text = text;
		this.isCorrect = isCorrect;
		this.question = question;
	}

	public Long getAnswerId() {
		return answerId;
	}

	public void setAnswerId(Long answerId) {
		this.answerId = answerId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}
}
