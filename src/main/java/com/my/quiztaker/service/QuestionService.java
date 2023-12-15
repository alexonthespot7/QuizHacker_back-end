package com.my.quiztaker.service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
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
	private UserRepository userRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private CommonService commonService;

	// Method to get list of questions for the quiz: if the user is authenticated
	// and is the quiz's owner they can see correct answers, otherwise they cannot
	public List<Question> getQuestionsByQuizId(Long quizId, Authentication auth) {
		Quiz quiz = commonService.checkQuizById(quizId);

		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		if (this.isAuthenticatedAndQuizOwner(quizId, auth, quiz))
			return questions;

		return this.hideCorrectAnswers(questions);
	}

	// Method to save questions and answers for these questions:
	public ResponseEntity<?> saveQuestions(Long quizId, List<Question> questions, Authentication auth) {
		Quiz quizInDB = commonService.checkQuizById(quizId);

		Long userIdOfQuizInDB = quizInDB.getUser().getId();

		commonService.checkAuthenticationAndRights(auth, userIdOfQuizInDB);

		this.saveQuestions(questions);

		return new ResponseEntity<>("Everything was saved successfully", HttpStatus.OK);
	}

	// Method to delete question by id and authentication instance:
	public ResponseEntity<?> deleteQuestionById(Long questionId, Authentication auth) {
		Question question = commonService.findQuestionById(questionId);

		Long idOfQuestionOwner = question.getQuiz().getUser().getId();
		
		commonService.checkAuthenticationAndRights(auth, idOfQuestionOwner);

		questionRepository.deleteById(questionId);

		return new ResponseEntity<>("Question was deleted successfully", HttpStatus.OK);
	}

	private boolean isAuthenticatedAndQuizOwner(Long quizId, Authentication auth, Quiz quiz) {
		if (auth == null || !(auth.getPrincipal() instanceof MyUser)) {
			return false;
		}

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optionalUser = userRepository.findByUsername(myUser.getUsername());

		return optionalUser.map(user -> user.getId().equals(quiz.getUser().getId())).orElse(false);
	}

	private List<Question> hideCorrectAnswers(List<Question> questions) {
		questions.forEach(question -> question.getAnswers().forEach(answer -> answer.setCorrect(false)));
		return questions;
	}

	private void saveQuestions(List<Question> questions) {
		Long questionId;

		for (Question question : questions) {
			// If the question wasn't in database before its id is under 0. Then question
			// should be created and added to database from scratch. Otherwise the program
			// changes already existing question and its answers
			questionId = question.getQuestionId();
			if (questionId < 0) {
				this.createNewQuestionWithAnswers(question);
			} else {
				this.updateExistingQuestionAndAnswers(question, questionId);
			}
		}
	}

	private void createNewQuestionWithAnswers(Question question) {
		Question newQuestion = new Question(question.getText(), question.getQuiz());
		questionRepository.save(newQuestion);

		List<Answer> answers = question.getAnswers();

		for (Answer answer : answers) {
			this.createNewAnswer(answer, newQuestion);
		}
	}

	private void createNewAnswer(Answer answer, Question question) {
		Answer newAnswer = new Answer(answer.getText(), answer.isCorrect(), question);
		answerRepository.save(newAnswer);
	}

	private void updateExistingQuestionAndAnswers(Question updatedQuestion, Long questionId) {
		Question newQuestion = commonService.findQuestionById(questionId);

		newQuestion.setText(updatedQuestion.getText());
		questionRepository.save(newQuestion);

		List<Answer> answers = updatedQuestion.getAnswers();

		for (Answer answer : answers) {
			this.updateExistingAnswer(answer);
		}

	}

	private void updateExistingAnswer(Answer updatedAnswer) {
		Long answerId = updatedAnswer.getAnswerId();
		Answer newAnswer = commonService.findAnswerById(answerId);

		newAnswer.setText(updatedAnswer.getText());
		newAnswer.setCorrect(updatedAnswer.isCorrect());
		answerRepository.save(newAnswer);
	}


}
