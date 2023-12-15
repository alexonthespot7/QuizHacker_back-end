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

	@RequestMapping(value = "/deletequestion/{questionid}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuestionById(@PathVariable("questionid") Long questionId, Authentication auth) {
		
		return questionService.deleteQuestionById(questionId, auth);
		
	}

	@RequestMapping(value = "/publishquiz/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> publishQuiz(@PathVariable("quizid") Long quizId, Authentication auth) {
		
		return quizService.publishQuiz(quizId, auth);
		
	}

	@RequestMapping(value = "/sendattempt/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> sendAttempt(@RequestBody AttemptForm attemptForm, @PathVariable("quizid") Long quizId,
			Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED);

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());
		Optional<Quiz> optQuiz = quizRepository.findById(quizId);

		if (!optUser.isPresent())
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED);

		User user = optUser.get();

		if (!optQuiz.isPresent())
			return new ResponseEntity<>("Quiz was not found for provided ID", HttpStatus.BAD_REQUEST);

		Quiz quiz = optQuiz.get();

		if (quiz.getUser().getId() == user.getId())
			return new ResponseEntity<>("It's impossible to send attempt for your own quiz", HttpStatus.CONFLICT); // 409

		List<AttemptAnswer> attemptAnswers = attemptForm.getAttemptAnswers();
		List<Question> questionsOfQuiz = quiz.getQuestions();

		if (questionsOfQuiz.size() != attemptAnswers.size())
			return new ResponseEntity<>(
					"The amount of answers in the request body doesn't match the amount of questions in the quiz",
					HttpStatus.BAD_REQUEST);

		int score = 0;
		Optional<Question> optionalQuestion;
		Optional<Answer> optionalAnswer;
		Question question;
		Answer answer;
		for (AttemptAnswer attemptAnswer : attemptAnswers) {
			optionalQuestion = questionRepository.findById(attemptAnswer.getQuestionId());
			if (!optionalQuestion.isPresent())
				return new ResponseEntity<>("Question not found", HttpStatus.BAD_REQUEST);

			question = optionalQuestion.get();
			optionalAnswer = answerRepository.findById(attemptAnswer.getAnswerId());

			if (!optionalAnswer.isPresent())
				return new ResponseEntity<>("Answer not found", HttpStatus.BAD_REQUEST);

			answer = optionalAnswer.get();

			if (!answer.getQuestion().equals(question))
				return new ResponseEntity<>("One or more answers don't match corresponding question",
						HttpStatus.BAD_REQUEST);

			if (question.getQuiz().getQuizId() != quizId)
				return new ResponseEntity<>("Some of questions are not in the corresponding quiz",
						HttpStatus.BAD_REQUEST);

			if (answer.isCorrect())
				score += 1;
		}

		Attempt attempt = new Attempt(score, quiz, user, attemptForm.getRating());

		attemptRepository.save(attempt);

		return ResponseEntity.ok().header(HttpHeaders.HOST, Integer.toString(score))
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
	}

	@RequestMapping(value = "/deletequiz/{quizid}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuizById(@PathVariable("quizid") Long quizId, Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());
		Optional<Quiz> optQuiz = quizRepository.findById(quizId);

		if (!optUser.isPresent())
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;

		User user = optUser.get();

		if (!optQuiz.isPresent())
			return new ResponseEntity<>("The quiz was not found for provided ID", HttpStatus.BAD_REQUEST);

		Quiz quiz = optQuiz.get();

		if (user.getId() != quiz.getUser().getId())
			return new ResponseEntity<>("You can't delete someone else's quiz", HttpStatus.UNAUTHORIZED); // 401;

		quizRepository.deleteById(quizId);

		return new ResponseEntity<>("Quiz was deleted successfully", HttpStatus.OK);
	}

	@RequestMapping(value = "/getavatar/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody String getAvatarByUserId(@PathVariable("userid") Long userId, Authentication auth) {

		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization problems");

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

		if (!optUser.isPresent())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization problems");

		User user = optUser.get();

		if (user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You can't get the avatarURL of other user");

		return user.getAvatarUrl();

	}

	@RequestMapping(value = "/updateavatar/{userid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateAvatarByUserId(@PathVariable("userid") Long userId, @RequestBody String url,
			Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

		if (!optUser.isPresent())
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

		User user = optUser.get();

		if (user.getId() != userId)
			return new ResponseEntity<>("You can't change someone else's avatarUrl", HttpStatus.UNAUTHORIZED);

		user.setAvatarUrl(url);

		userRepository.save(user);

		return new ResponseEntity<>("The avatar url was updated successfully", HttpStatus.OK);

	}

	private int findPosition(String username, List<UserPublic> leaders) {
		int position = -1;
		UserPublic userPublic;
		String usernameOfCurrentUser;

		for (int i = 0; i < leaders.size(); i++) {
			userPublic = leaders.get(i);
			usernameOfCurrentUser = userPublic.getUsername();

			if (usernameOfCurrentUser.equals(username)) {
				position = i + 1;
				break;
			}
		}

		return position;
	}
}
