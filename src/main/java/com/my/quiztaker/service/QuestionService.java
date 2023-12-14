package com.my.quiztaker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@Service
public class QuestionService {
	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private UserRepository userRepository;

	// Method to get list of questions for the quiz: if the user is authenticated
	// and is the quiz's owner they can see correct answers, otherwise they cannot
	public List<Question> getQuestionsByQuizId(Long quizId, Authentication auth) {
		Quiz quiz = this.checkQuizById(quizId);

		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		if (this.isAuthenticatedAndQuizOwner(quizId, auth, quiz))
			return questions;

		return this.hideCorrectAnswers(questions);
	}

	private Quiz checkQuizById(Long quizId) {
		Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

		if (!optionalQuiz.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There's no quiz with this id");

		Quiz quiz = optionalQuiz.get();

		return quiz;
	}

	private boolean isAuthenticatedAndQuizOwner(Long quizId, Authentication auth, Quiz quiz) {
		if (auth == null) {
			return false;
		}

		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			return false;
		}

		MyUser myUser = (MyUser) auth.getPrincipal();
		String username = myUser.getUsername();
		Optional<User> optUser = userRepository.findByUsername(username);

		if (!optUser.isPresent()) {
			return false;
		}

		Long userId = myUser.getId();
		if (userId != quiz.getUser().getId()) {
			return false;
		}

		return true;
	}

	private List<Question> hideCorrectAnswers(List<Question> questions) {
		List<Answer> answers;
		for (Question question : questions) {

			answers = question.getAnswers();

			for (Answer answer : answers) {
				answer.setCorrect(false);
			}
		}

		return questions;
	}
	
	
}
