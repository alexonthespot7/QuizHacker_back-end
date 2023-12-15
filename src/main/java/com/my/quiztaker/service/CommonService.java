package com.my.quiztaker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
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

	// Method to check user's authentication and rights to read information
	public User checkAuthentication(Authentication auth, Long userId) {
		if (!(auth.getPrincipal() instanceof MyUser)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
		}

		MyUser myUser = (MyUser) auth.getPrincipal();
		String username = myUser.getUsername();
		Optional<User> optionalUser = userRepository.findByUsername(username);

		User user = optionalUser.filter(localUser -> localUser.getId().equals(userId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"You are not allowed to get someone else's info"));
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
}
