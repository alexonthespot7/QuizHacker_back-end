package com.my.quiztaker.forms;

import java.util.List;

public class AttemptForm {
	private List<AttemptAnswer> attemptAnswers;
	private Integer rating;
	
	public AttemptForm() {}
	
	public AttemptForm(List<AttemptAnswer> attemptAnswers, Integer rating) {
		this.attemptAnswers = attemptAnswers;
		this.rating = rating;
	}

	public List<AttemptAnswer> getAttemptAnswers() {
		return attemptAnswers;
	}
	public void setAttemptAnswers(List<AttemptAnswer> attemptAnswers) {
		this.attemptAnswers = attemptAnswers;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
}
