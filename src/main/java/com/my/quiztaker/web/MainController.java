package com.my.quiztaker.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

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
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.AttemptForm;
import com.my.quiztaker.forms.LeaderboardAuthorized;
import com.my.quiztaker.forms.PersonalInfo;
import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.QuizUpdate;
import com.my.quiztaker.forms.UserPublic;

@RestController
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository urepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private AttemptRepository attRepository;

	@Autowired
	private QuestionRepository questRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository catRepository;

	@Autowired
	private DifficultyRepository difRepository;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	

	@RequestMapping(value = "/deletequestion/{questionid}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuestionById(@PathVariable("questionid") Long questionId, Authentication auth) {
		Optional<Question> optQuestion = questRepository.findById(questionId);
		if (optQuestion.isPresent()) {
			if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
				MyUser myUser = (MyUser) auth.getPrincipal();
				Optional<User> optUser = urepository.findByUsername(myUser.getUsername());

				if (optUser.isPresent() && optUser.get().getId() == optQuestion.get().getQuiz().getUser().getId()) {
					questRepository.delete(optQuestion.get());

					return new ResponseEntity<>("Question was deleted successfully", HttpStatus.OK);
				} else {
					return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
				}
			} else {
				return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401
			}
		} else {
			return new ResponseEntity<>("There is no such question", HttpStatus.BAD_REQUEST); // 400
		}
	}

	@RequestMapping(value = "/publishquiz/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> publishQuiz(@PathVariable("quizid") Long quizId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());
			Optional<Quiz> optQuiz = quizRepository.findById(quizId);

			if (optUser.isPresent() && optQuiz.isPresent()
					&& optUser.get().getId() == optQuiz.get().getUser().getId()) {
				Quiz quiz = optQuiz.get();
				quiz.setStatus("Published");
				quizRepository.save(quiz);

				return new ResponseEntity<>("Quiz was published successfully", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Authorization problems or quiz does not exist", HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/sendattempt/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> sendAttempt(@RequestBody AttemptForm attemptForm, @PathVariable("quizid") Long quizId,
			Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());
			Optional<Quiz> optQuiz = quizRepository.findById(quizId);

			if (optUser.isPresent() && optQuiz.isPresent()) {
				Quiz quiz = optQuiz.get();
				User user = optUser.get();

				int score = 0;

				for (int i = 0; i < attemptForm.getAttemptAnswers().size(); i++) {
					if (answerRepository.findById(attemptForm.getAttemptAnswers().get(i).getAnswerId()).get()
							.isCorrect())
						score += 1;
				}

				Attempt attempt = new Attempt(score, quiz, user, attemptForm.getRating());

				attRepository.save(attempt);

				return ResponseEntity.ok().header(HttpHeaders.HOST, Integer.toString(score))
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
			} else {
				return new ResponseEntity<>("Authorization problems or quiz does not exist", HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/deletequiz/{quizid}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuizById(@PathVariable("quizid") Long quizId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());
			Optional<Quiz> optQuiz = quizRepository.findById(quizId);
			if (optUser.isPresent() && optQuiz.isPresent()
					&& optUser.get().getId() == optQuiz.get().getUser().getId()) {
				Quiz quiz = optQuiz.get();

				quizRepository.delete(quiz);

				return new ResponseEntity<>("Quiz was deleted successfully", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401
		}
	}

	@RequestMapping(value = "/getavatar/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody String getAvatarByUserId(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				return optUser.get().getAvatarUrl();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@RequestMapping(value = "/updateavatar/{userid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateAvatarByUserId(@PathVariable("userid") Long userId, @RequestBody String url,
			Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				User user = optUser.get();
				user.setAvatarUrl(url);

				urepository.save(user);

				return new ResponseEntity<>("The avatar url was updated successfully", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		}
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
