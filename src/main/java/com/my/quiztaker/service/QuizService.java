package com.my.quiztaker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;

@Service
public class QuizService {
	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private QuestionRepository questionRepository;

	// Method to get published quizzes with its rating and amount of questions;
	public List<QuizRatingQuestions> getQuizzes() {
		List<Quiz> publishedQuizzes = (List<Quiz>) quizRepository.findAllPublished();

		// Using list of quizzes to create list of quizzes with its rating and amount of
		// questions
		List<QuizRatingQuestions> quizRatingQuestionsList = this
				.makeQuizRatingQuestionsListFromQuizzes(publishedQuizzes);

		return quizRatingQuestionsList;
	}

	private List<QuizRatingQuestions> makeQuizRatingQuestionsListFromQuizzes(List<Quiz> quizzes) {
		Double rating;
		QuizRatingQuestions quizRatingQuestions;
		Integer questionsAmount;
		Long quizId;

		List<QuizRatingQuestions> quizRatingsQuestionsList = new ArrayList<QuizRatingQuestions>();

		for (Quiz quiz : quizzes) {
			quizId = quiz.getQuizId();

			rating = attemptRepository.findQuizRating(quizId);
			questionsAmount = this.getAmountOfQuestionsInQuiz(quizId);

			quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questionsAmount);
			quizRatingsQuestionsList.add(quizRatingQuestions);
		}

		return quizRatingsQuestionsList;
	}

	private Integer getAmountOfQuestionsInQuiz(Long quizId) {
		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		Integer questionsAmount = questions.size();

		return questionsAmount;
	}

	// The method to get quiz info and its rating by quiz ID:
	public QuizRating getQuizById(Long quizId) {
		Quiz quiz = this.checkQuizById(quizId);

		Double rating = attemptRepository.findQuizRating(quizId);

		return new QuizRating(quiz, rating);
	}

	private Quiz checkQuizById(Long quizId) {
		Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

		if (!optionalQuiz.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There's no quiz with this id");

		Quiz quiz = optionalQuiz.get();
		
		return quiz;
	}
}
