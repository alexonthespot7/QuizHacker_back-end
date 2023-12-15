package com.my.quiztaker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.AttemptAnswer;
import com.my.quiztaker.forms.AttemptForm;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
import com.my.quiztaker.model.Attempt;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@Service
public class AttemptService {
	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private CommonService commonService;

	// Method to save attempt to database and return the score to the client-side:
	public ResponseEntity<?> sendAttempt(AttemptForm attemptForm, Long quizId, Authentication auth) {
		Quiz quiz = commonService.checkQuizById(quizId);
		Long idOfQuizOwner = quiz.getUser().getId();

		User user = commonService.checkAuthentication(auth);
		Long userId = user.getId();

		if (idOfQuizOwner == userId)
			return new ResponseEntity<>("It's impossible to send attempt for your own quiz", HttpStatus.CONFLICT); // 409

		List<AttemptAnswer> attemptAnswers = this.checkAnswersSizeEqualsQuestionsSize(attemptForm, quiz);

		int rating = attemptForm.getRating();

		return this.saveAttempt(quiz, user, rating, attemptAnswers, quizId);
	}

	private List<AttemptAnswer> checkAnswersSizeEqualsQuestionsSize(AttemptForm attemptForm, Quiz quiz) {
		List<AttemptAnswer> attemptAnswers = attemptForm.getAttemptAnswers();
		List<Question> questionsOfQuiz = quiz.getQuestions();

		if (questionsOfQuiz.size() != attemptAnswers.size())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"The amount of answers in the request body doesn't match the amount of questions in the quiz");

		return attemptAnswers;
	}

	private ResponseEntity<?> saveAttempt(Quiz quiz, User user, int rating, List<AttemptAnswer> attemptAnswers,
			Long quizId) {
		int score = this.findScore(attemptAnswers, quizId);

		Attempt attempt = new Attempt(score, quiz, user, rating);

		attemptRepository.save(attempt);

		return ResponseEntity.ok().header(HttpHeaders.HOST, Integer.toString(score))
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
	}

	private int findScore(List<AttemptAnswer> attemptAnswers, Long quizId) {
		int score = 0;
		Question question;
		Answer answer;
		
		for (AttemptAnswer attemptAnswer : attemptAnswers) {
			question = commonService.findQuestionById(attemptAnswer.getQuestionId());

			answer = commonService.findAnswerById(attemptAnswer.getAnswerId());

			this.validateAttemptAnswer(question, answer, quizId);

			if (answer.isCorrect())
				score += 1;
		}

		return score;
	}
	
	private void validateAttemptAnswer(Question question, Answer answer, Long quizId) {
		if (!answer.getQuestion().equals(question)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	            "One or more answers don't match corresponding question");
	    }

	    if (question.getQuiz().getQuizId() != quizId) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	            "Some of questions are not in the corresponding quiz");
	    }
	}
}
