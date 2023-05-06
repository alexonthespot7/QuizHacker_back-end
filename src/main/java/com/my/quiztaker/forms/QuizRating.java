package com.my.quiztaker.forms;

import com.my.quiztaker.model.Quiz;

public class QuizRating {
	private Quiz quiz;
	private Double rating;
	
	public QuizRating(Quiz quiz, Double rating) {
		this.quiz = quiz;
		this.rating = rating;
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
	
	
}
