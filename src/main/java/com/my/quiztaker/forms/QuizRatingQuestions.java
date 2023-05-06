package com.my.quiztaker.forms;

import com.my.quiztaker.model.Quiz;

public class QuizRatingQuestions {
	private Quiz quiz;
	private Double rating;
	private Integer questions;
	
	public QuizRatingQuestions(Quiz quiz, Double rating, Integer questions) {
		this.quiz = quiz;
		this.rating = rating;
		this.questions = questions;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Integer getQuestions() {
		return questions;
	}

	public void setQuestions(Integer questions) {
		this.questions = questions;
	}
}
