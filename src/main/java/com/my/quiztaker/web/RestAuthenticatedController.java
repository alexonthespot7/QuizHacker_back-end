package com.my.quiztaker.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.AttemptAnswer;
import com.my.quiztaker.forms.AttemptForm;
import com.my.quiztaker.forms.Leaderboard;
import com.my.quiztaker.forms.PersonalInfo;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.QuizUpdate;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
import com.my.quiztaker.model.Attempt;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.service.AttemptService;
import com.my.quiztaker.service.AuthenticationService;
import com.my.quiztaker.service.QuestionService;
import com.my.quiztaker.service.QuizService;
import com.my.quiztaker.service.UserService;

@RestController
public class RestAuthenticatedController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private QuestionService questionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private QuizService quizService;
	
	@Autowired
	private AttemptService attemptService;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	@GetMapping("/questions/{quizid}")
	public @ResponseBody List<Question> getQuestionsByQuizId(@PathVariable("quizid") Long quizId, Authentication auth) {

		return questionService.getQuestionsByQuizId(quizId, auth);
		
	}

	@GetMapping("/users/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody PersonalInfo getPersonalInfo(@PathVariable("userid") Long userId, Authentication auth) {
		
		return userService.getPersonalInfo(userId, auth);
	
	}

	@GetMapping("/usersauth/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody Leaderboard getLeaderboardAuth(@PathVariable("userid") Long userId, Authentication auth) {
		
		return userService.getLeaderboardAuth(userId, auth);
		
	}

	@GetMapping("/quizzesbyuser/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> getQuizzesOfOthersAuth(@PathVariable("userid") Long userId,
			Authentication auth) {
		
		return quizService.getQuizzesOfOthersAuth(userId, auth);
		
	}

	@GetMapping("/personalquizzes/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> getPersonalQuizzes(@PathVariable("userid") Long userId,
			Authentication auth) {
		
		return quizService.getPersonalQuizzes(userId, auth);
		
	}

	@PostMapping(value = "/createquiz")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createQuizByAuth(Authentication auth) {
		
		return quizService.createQuizByAuth(auth);
		
	}

	@PutMapping("/updatequiz/{quizid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateQuizByAuth(@PathVariable("quizid") Long quizId, @RequestBody QuizUpdate quizUpdated,
			Authentication auth) {
		
		return quizService.updateQuizByAuth(quizId, quizUpdated, auth);
		
	}

	@PutMapping("/savequestions/{quizid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> saveQuestions(@PathVariable("quizid") Long quizId, @RequestBody List<Question> questions,
			Authentication auth) {
		
		return questionService.saveQuestions(quizId, questions, auth);
		
	}

	@DeleteMapping("/deletequestion/{questionid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuestionById(@PathVariable("questionid") Long questionId, Authentication auth) {
		
		return questionService.deleteQuestionById(questionId, auth);
		
	}

	@PutMapping("/publishquiz/{quizid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> publishQuiz(@PathVariable("quizid") Long quizId, Authentication auth) {
		
		return quizService.publishQuiz(quizId, auth);
		
	}

	@PostMapping("/sendattempt/{quizid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> sendAttempt(@RequestBody AttemptForm attemptForm, @PathVariable("quizid") Long quizId,
			Authentication auth) {
		
		return attemptService.sendAttempt(attemptForm, quizId, auth);
		
	}

	@DeleteMapping("/deletequiz/{quizid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuizById(@PathVariable("quizid") Long quizId, Authentication auth) {
		
		return quizService.deleteQuizById(quizId, auth);
		
	}

	@GetMapping("/getavatar/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody String getAvatarByUserId(@PathVariable("userid") Long userId, Authentication auth) {

		return userService.getAvatarByUserId(userId, auth);

	}

	@PutMapping("/updateavatar/{userid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateAvatarByUserId(@PathVariable("userid") Long userId, @RequestBody String url,
			Authentication auth) {
		
		return userService.updateAvatarByUserId(userId, url, auth);

	}
}
