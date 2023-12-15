package com.my.quiztaker.forms;

public class AttemptAnswer {
	private Long answerId;
	private Long questionId;
	
	public AttemptAnswer() {}
	
	public AttemptAnswer(Long answerId, Long questionId) {
		this.answerId = answerId;
		this.questionId = questionId;
	}

	public Long getAnswerId() {
		return answerId;
	}
	public void setAnswerId(Long answerId) {
		this.answerId = answerId;
	}
	public Long getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
}
