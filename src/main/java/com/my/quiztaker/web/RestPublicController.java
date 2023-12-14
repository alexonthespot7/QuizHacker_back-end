package com.my.quiztaker.web;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.Leaderboard;
import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.SignupCredentials;
import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.service.CategoryService;
import com.my.quiztaker.service.DifficultyService;
import com.my.quiztaker.service.QuizService;
import com.my.quiztaker.service.UserService;

import jakarta.mail.MessagingException;

@RestController
public class RestPublicController {
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private DifficultyService difficultyService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private UserService userService;
;

	@GetMapping("/categories")
	public @ResponseBody List<Category> getCategories() {

		return categoryService.getCategories();

	}

	@GetMapping("/difficulties")
	public @ResponseBody List<Difficulty> getDifficulties() {

		return difficultyService.getDifficulties();

	}

	@GetMapping("/quizzes")
	public @ResponseBody List<QuizRatingQuestions> getQuizzes() {

		return quizService.getQuizzes();

	}

	@GetMapping("/users")
	public @ResponseBody Leaderboard getLeaderboardNoAuth() {

		return userService.getLeaderboardNoAuth();

	}

	@GetMapping("/quizzes/{quizid}")
	public @ResponseBody QuizRating getQuizById(@PathVariable("quizid") Long quizId) {

		return quizService.getQuizById(quizId);

	}

	@PostMapping("/login")
	public ResponseEntity<?> getToken(@RequestBody AccountCredentials credentials)
			throws UnsupportedEncodingException, MessagingException {

		return userService.getToken(credentials);

	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignupCredentials creds)
			throws UnsupportedEncodingException, MessagingException {
		
		return userService.signUp(creds);
		
	}

	@PutMapping("/verify/{userid}")
	public ResponseEntity<?> verifyUser(@RequestBody String token, @PathVariable("userid") Long userId) {
		
		return userService.verifyUser(token, userId);
		
	}

	@PutMapping("/resetpassword")
	public ResponseEntity<?> resetPassword(@RequestBody String email)
			throws UnsupportedEncodingException, MessagingException {
		
		return userService.resetPassword(email);
	
	}
}
