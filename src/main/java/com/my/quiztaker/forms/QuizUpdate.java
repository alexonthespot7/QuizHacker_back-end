package com.my.quiztaker.forms;

public class QuizUpdate {
	private Long quizId;
	private String title;
	private String description;
	private Long difficulty;
	private Integer minutes;
	private Double rating;
	private String status;
	private Long user;
	private Long category;

	public QuizUpdate() {}

	public QuizUpdate(Long quizId, String title, String description, Long difficulty, Integer minutes, Double rating,
			String status, Long user, Long category) {
		this.quizId = quizId;
		this.title = title;
		this.description = description;
		this.difficulty = difficulty;
		this.minutes = minutes;
		this.rating = rating;
		this.status = status;
		this.user = user;
		this.category = category;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
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

	public Long getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Long difficulty) {
		this.difficulty = difficulty;
	}

	public Integer getMinutes() {
		return minutes;
	}

	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

}
