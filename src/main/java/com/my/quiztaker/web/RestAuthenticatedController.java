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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.PersonalInfo;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.QuizUpdate;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
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
	private AuthenticationService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	@RequestMapping("/questions/{quizid}")
	public @ResponseBody List<Question> getQuestionsByQuizId(@PathVariable("quizid") Long quizId, Authentication auth) {

		Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

		if (!optionalQuiz.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There's no quiz with this id");

		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);
		if (auth == null) {
			for (int i = 0; i < questions.size(); i++) {
				for (int j = 0; j < questions.get(i).getAnswers().size(); j++) {
					questions.get(i).getAnswers().get(j).setCorrect(false);
				}
			}
		} else {
			if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
				MyUser myUser = (MyUser) auth.getPrincipal();
				Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

				if (optUser.isPresent()
						&& optUser.get().getId() == quizRepository.findById(quizId).get().getUser().getId()) {

					return questionRepository.findQuestionsByQuizId(quizId);
				} else {
					for (int i = 0; i < questions.size(); i++) {
						for (int j = 0; j < questions.get(i).getAnswers().size(); j++) {
							questions.get(i).getAnswers().get(j).setCorrect(false);
						}
					}
				}
			} else {
				for (int i = 0; i < questions.size(); i++) {
					for (int j = 0; j < questions.get(i).getAnswers().size(); j++) {
						questions.get(i).getAnswers().get(j).setCorrect(false);
					}
				}
			}
		}
		return questions;
	}

	@RequestMapping("/users/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody PersonalInfo getLeaderboardAuth(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());
			if (optUser.isPresent() && optUser.get().getId() == userId) {
				UserPublic userPublic = userRepository.findRatingByUserId(userId);

				// if user doesn't have any attempts
				if (userPublic == null) {
					return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), 0,
							attemptRepository.findAttemptsByUserId(userId), -1);
				}

				String username = optUser.get().getUsername();
				List<UserPublic> leaders = userRepository.findLeaderBoard();
				int position = this.findPosition(username, leaders);

				return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), userPublic.getRating(),
						attemptRepository.findAttemptsByUserId(userId), position);
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
		}
	}

	@RequestMapping("/quizzesbyuser/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> getQuizzesAuth(@PathVariable("userid") Long userId,
			Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				List<Quiz> quizzesOfOthers = quizRepository.findPublishedQuizzesFromOtherUsers(userId);
				Double rating;
				QuizRatingQuestions quizRatingQuestions;
				Integer questions;

				List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

				for (Quiz quiz : quizzesOfOthers) {
					if (attemptRepository.findAttemptsForTheQuizByUserId(userId, quiz.getQuizId()) == 0) {
						rating = attemptRepository.findQuizRating(quiz.getQuizId());
						questions = questionRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
						quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);

						quizRatingsQuestionss.add(quizRatingQuestions);
					}
				}

				return quizRatingsQuestionss;
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
		}
	}

	@RequestMapping("/personalquizzes/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> getPersonalQuizzes(@PathVariable("userid") Long userId,
			Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				List<Quiz> quizzesOfUser = quizRepository.findQuizzesByUserId(userId);
				Double rating;
				QuizRatingQuestions quizRatingQuestions;
				Integer questions;

				List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

				for (Quiz quiz : quizzesOfUser) {
					rating = attemptRepository.findQuizRating(quiz.getQuizId());
					questions = questionRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
					quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);

					quizRatingsQuestionss.add(quizRatingQuestions);
				}

				return quizRatingsQuestionss;
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
		}
	}

	@RequestMapping(value = "/createquiz")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createQuizByAuth(Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent()) {
				User user = optUser.get();
				Quiz quiz = new Quiz(user, categoryRepository.findByName("Other").get(),
						difficultyRepository.findByName("Hard").get());
				quizRepository.save(quiz);

				Question defaultQuestion1 = new Question(quiz);
				Question defaultQuestion2 = new Question(quiz);
				questionRepository.save(defaultQuestion1);
				questionRepository.save(defaultQuestion2);

				Answer defaultAnswer1 = new Answer(defaultQuestion1, true);
				Answer defaultAnswer2 = new Answer(defaultQuestion1, false);
				Answer defaultAnswer3 = new Answer(defaultQuestion1, false);
				Answer defaultAnswer4 = new Answer(defaultQuestion1, false);

				Answer defaultAnswer5 = new Answer(defaultQuestion2, true);
				Answer defaultAnswer6 = new Answer(defaultQuestion2, false);
				Answer defaultAnswer7 = new Answer(defaultQuestion2, false);
				Answer defaultAnswer8 = new Answer(defaultQuestion2, false);

				answerRepository.save(defaultAnswer1);
				answerRepository.save(defaultAnswer2);
				answerRepository.save(defaultAnswer3);
				answerRepository.save(defaultAnswer4);
				answerRepository.save(defaultAnswer5);
				answerRepository.save(defaultAnswer6);
				answerRepository.save(defaultAnswer7);
				answerRepository.save(defaultAnswer8);

				return ResponseEntity.ok().header(HttpHeaders.HOST, quiz.getQuizId().toString())
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
			} else {
				return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401
		}
	}

	@RequestMapping(value = "/updatequiz/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateQuizByAuth(@PathVariable("quizid") Long quizId, @RequestBody QuizUpdate quiz,
			Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

		User user = optUser.get();

		if (!optUser.isPresent())
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED);

		Optional<Quiz> optionalQuizInDB = quizRepository.findById(quizId);

		if (!optionalQuizInDB.isPresent())
			return new ResponseEntity<>("There is no such quiz", HttpStatus.BAD_REQUEST); // 400;

		if (quizId != quiz.getQuizId())
			return new ResponseEntity<>(
					"The id missmatch: provided in the path id doesn't equal the id of the quiz in request body",
					HttpStatus.BAD_REQUEST); // 400;

		Long userId = user.getId();
		Quiz currentQuiz = optionalQuizInDB.get();
		Long userIdOfQuizInDB = currentQuiz.getUser().getId();

		if (userId != userIdOfQuizInDB)
			return new ResponseEntity<>("You are not allowed to change someone else's quiz", HttpStatus.UNAUTHORIZED); // 401;

		Optional<Category> optionalCategory = categoryRepository.findById(quiz.getCategory());

		if (!optionalCategory.isPresent())
			return new ResponseEntity<>("The category wasn't find by provided ID", HttpStatus.BAD_REQUEST); // 400;

		Optional<Difficulty> optionalDifficulty = difficultyRepository.findById(quiz.getDifficulty());

		if (!optionalDifficulty.isPresent())
			return new ResponseEntity<>("The difficulty wasn't find by provided ID", HttpStatus.BAD_REQUEST); // 400;

		Category category = optionalCategory.get();
		Difficulty difficulty = optionalDifficulty.get();

		currentQuiz.setCategory(category);
		currentQuiz.setDifficulty(difficulty);
		currentQuiz.setDescription(quiz.getDescription());
		currentQuiz.setMinutes(quiz.getMinutes());
		currentQuiz.setTitle(quiz.getTitle());
		quizRepository.save(currentQuiz);

		return new ResponseEntity<>("Quiz info was updated successfully", HttpStatus.OK);
	}

	@RequestMapping(value = "/savequestions/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> saveQuestions(@PathVariable("quizid") Long quizId, @RequestBody List<Question> questions,
			Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optUser = userRepository.findByUsername(myUser.getUsername());

		if (!optUser.isPresent())
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;

		User user = optUser.get();
		Optional<Quiz> optionalQuizInDB = quizRepository.findById(quizId);

		if (!optionalQuizInDB.isPresent())
			return new ResponseEntity<>("No quiz was found for provided ID", HttpStatus.BAD_REQUEST); // 400;

		Quiz quizInDB = optionalQuizInDB.get();

		if (user.getId() != quizInDB.getUser().getId())
			return new ResponseEntity<>("You are not allowed to change someone else's quiz", HttpStatus.UNAUTHORIZED); // 401;

		Question newQuestion;
		Answer newAnswer;
		Iterator<Question> iterator = questions.iterator();

		while (iterator.hasNext()) {
			Question question = iterator.next();
			// If the question wasn't in database before its id is under 0. Then question
			// should be created and added to database from scratch. Otherwise the program
			// changes already existing question and its answers
			if (question.getQuestionId() < 0) {
				newQuestion = new Question(question.getText(), question.getQuiz());
				questionRepository.save(newQuestion);
				for (int i = 0; i < question.getAnswers().size(); i++) {
					newAnswer = new Answer(question.getAnswers().get(i).getText(),
							question.getAnswers().get(i).isCorrect(), newQuestion);
					answerRepository.save(newAnswer);
				}
			} else {
				for (int i = 0; i < question.getAnswers().size(); i++) {
					newAnswer = answerRepository.findById(question.getAnswers().get(i).getAnswerId()).get();

					newAnswer.setText(question.getAnswers().get(i).getText());
					newAnswer.setCorrect(question.getAnswers().get(i).isCorrect());
					answerRepository.save(newAnswer);
				}
				newQuestion = questionRepository.findById(question.getQuestionId()).get();

				newQuestion.setText(question.getText());
				questionRepository.save(newQuestion);
			}
		}

		return new ResponseEntity<>("Everything was saved successfully", HttpStatus.OK);
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
