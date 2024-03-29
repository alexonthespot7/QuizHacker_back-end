package com.my.quiztaker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class CommonService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	// Method to check user's authentication and rights to read information. If user
	// is authenticated and authorized then the user instance is returned
	public User checkAuthenticationAndRights(Authentication auth, Long userId) {
		User user = this.checkAuthentication(auth);

		if (user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"You are not allowed to get someone else's info");

		return user;
	}

	// Method to check user's authentication
	public User checkAuthentication(Authentication auth) {
		if (!(auth.getPrincipal() instanceof MyUser))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		MyUser myUser = (MyUser) auth.getPrincipal();
		String username = myUser.getUsername();
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		User user = optionalUser.get();

		return user;
	}

	// Method to check if there's a quiz in DB by provided ID:
	public Quiz checkQuizById(Long quizId) {
		Quiz quiz = quizRepository.findById(quizId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "There's no quiz with this id"));
		return quiz;
	}

	// Method to verify user;
	public void verifyUser(User user) {
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		userRepository.save(user);
	}

	// Method to check the status of quiz: user can't change published quizzes
	public void checkQuizStatus(Quiz quiz) {
		String quizStatus = quiz.getStatus();
		if (quizStatus.equals("Published"))
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"You can't update the quiz that is already published");
	}

	// Method to check if the question's in db by ID:
	public Question findQuestionById(Long questionId) {
		Optional<Question> optionalQuestion = questionRepository.findById(questionId);

		if (!optionalQuestion.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"One or more of your questions that supposed to be in db can't be found in db");

		Question question = optionalQuestion.get();

		return question;
	}

	// Method to check if the answer is in DB by ID:
	public Answer findAnswerById(Long answerId) {
		Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
		if (!optionalAnswer.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"One or more of your answers that supposed to be in can't be found in db");

		Answer answer = optionalAnswer.get();

		return answer;
	}
}
