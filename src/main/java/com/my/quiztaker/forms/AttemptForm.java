package com.my.quiztaker.forms;

import java.util.List;

public class AttemptForm {
	private List<AttemptAnswers> attemptAnswers;
	private Integer rating;
	public List<AttemptAnswers> getAttemptAnswers() {
		return attemptAnswers;
	}
	public void setAttemptAnswers(List<AttemptAnswers> attemptAnswers) {
		this.attemptAnswers = attemptAnswers;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
}
