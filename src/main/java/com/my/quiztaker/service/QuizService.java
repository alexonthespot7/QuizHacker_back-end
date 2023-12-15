package com.my.quiztaker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@Service
public class QuizService {
	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CommonService commonService;

	// Method to get published quizzes with its rating and amount of questions;
	public List<QuizRatingQuestions> getQuizzes() {
		List<Quiz> publishedQuizzes = (List<Quiz>) quizRepository.findAllPublished();

		// Using list of quizzes to create list of quizzes with its rating and amount of
		// questions
		List<QuizRatingQuestions> quizRatingQuestionsList = this
				.makeQuizRatingQuestionsListFromQuizzes(publishedQuizzes);

		return quizRatingQuestionsList;
	}

	// The method to get quiz info and its rating by quiz ID:
	public QuizRating getQuizById(Long quizId) {
		Quiz quiz = commonService.checkQuizById(quizId);

		Double rating = attemptRepository.findQuizRating(quizId);

		return new QuizRating(quiz, rating);
	}

	// Method to get published quizzes created by other users than the authenticated
	// one:
	public List<QuizRatingQuestions> getQuizzesOfOthersAuth(Long userId, Authentication auth) {
		commonService.checkAuthentication(auth, userId);

		List<Quiz> quizzesOfOthersNoAttempts = this.getQuizzesOfOthersThatUserDidntAttempt(userId);

		List<QuizRatingQuestions> quizRatingQuestionsList = this
				.makeQuizRatingQuestionsListFromQuizzes(quizzesOfOthersNoAttempts);

		return quizRatingQuestionsList;
	}

	private List<QuizRatingQuestions> makeQuizRatingQuestionsListFromQuizzes(List<Quiz> quizzes) {
		return quizzes.stream()
	            .map(this::makeQuizRatingQuestionsFromQuiz)
	            .collect(Collectors.toList());
	}

	private QuizRatingQuestions makeQuizRatingQuestionsFromQuiz(Quiz quiz) {
		Long quizId = quiz.getQuizId();

		Double rating = attemptRepository.findQuizRating(quizId);
		Integer questionsAmount = this.getAmountOfQuestionsInQuiz(quizId);

		QuizRatingQuestions quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questionsAmount);

		return quizRatingQuestions;
	}

	private Integer getAmountOfQuestionsInQuiz(Long quizId) {
		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		Integer questionsAmount = questions.size();

		return questionsAmount;
	}

	private List<Quiz> getQuizzesOfOthersThatUserDidntAttempt(Long userId) {
		List<Quiz> quizzesOfOthers = quizRepository.findPublishedQuizzesFromOtherUsers(userId);

		return quizzesOfOthers.stream()
				.filter(quiz -> attemptRepository.findAttemptsForTheQuizByUserId(userId, quiz.getQuizId()) == 0)
				.collect(Collectors.toList());
	}
}
