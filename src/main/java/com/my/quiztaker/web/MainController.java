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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
	@Autowired
	private UserRepository uRepository;

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

	@RequestMapping("/categories")
	public @ResponseBody List<Category> categoryListRest() {
		return (List<Category>) catRepository.findAll();
	}

	@RequestMapping("/difficulties")
	public @ResponseBody List<Difficulty> difficultyListRest() {
		return (List<Difficulty>) difRepository.findAll();
	}

	@RequestMapping("/quizzes")
	public @ResponseBody List<QuizRatingQuestions> quizListRest() {		
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		Double rating;
		QuizRatingQuestions quizRatingQuestions;
		Integer questions;
		
		List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

		for (Quiz quiz : quizzes) {
			rating = attRepository.findQuizRating(quiz.getQuizId());
			questions = questRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
			quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);
			
			quizRatingsQuestionss.add(quizRatingQuestions);
		}
		
		return quizRatingsQuestionss;
	}
	
	@RequestMapping("/users")
	public @ResponseBody LeaderboardAuthorized usersLeaderboard() {
		return new LeaderboardAuthorized(uRepository.findLeaderBoard(), -1);
	}
	
	@RequestMapping("/users/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody PersonalInfo restPersonalInfo(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());
			if (optUser.isPresent() && optUser.get().getId() == userId) {	
				UserPublic userPublic = uRepository.findRatingByUserId(userId);
				
				if (userPublic == null) {
					return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), 0.0, attRepository.findAttemptsByUserId(userId), uRepository.findPositionByRating(userId));
				}				
				return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), userPublic.getRating(), attRepository.findAttemptsByUserId(userId), uRepository.findPositionByRating(userId));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	@RequestMapping("/usersauth/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody LeaderboardAuthorized leaderboardAuthorized(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {				
				List<UserPublic> leaders = uRepository.findLeaderBoard();
				
				Integer position = uRepository.findPositionByRating(userId);
				
				if (position > 10) {
					UserPublic userRow = uRepository.findRatingByUserId(userId);
					leaders.add(userRow);
				}
				
				LeaderboardAuthorized leaderboardList = new LeaderboardAuthorized(leaders, position);
				
				return leaderboardList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	@RequestMapping("/quizzesbyuser/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> quizListRestAuthenticated(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
				Double rating;
				QuizRatingQuestions quizRatingQuestions;
				Integer questions;
				
				List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

				for (Quiz quiz : quizzes) {
					if (quiz.getUser().getId() != userId) {
						rating = attRepository.findQuizRating(quiz.getQuizId());
						questions = questRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
						quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);
						
						quizRatingsQuestionss.add(quizRatingQuestions);
					}
				}
				
				return quizRatingsQuestionss;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	@RequestMapping("/personalquizzes/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody List<QuizRatingQuestions> getPersonalQuizzes(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
				Double rating;
				QuizRatingQuestions quizRatingQuestions;
				Integer questions;
				
				List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

				for (Quiz quiz : quizzes) {
					if (quiz.getUser().getId() == userId) {
						rating = attRepository.findQuizRating(quiz.getQuizId());
						questions = questRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
						quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);
						
						quizRatingsQuestionss.add(quizRatingQuestions);
					}
				}
				
				return quizRatingsQuestionss;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@RequestMapping("/quizzes/{quizid}")
	public @ResponseBody QuizRating findQuizByIdRest(@PathVariable("quizid") Long quizId) {
		if (quizRepository.findById(quizId).isPresent()) {
			Quiz quiz = quizRepository.findById(quizId).get();
			Double rating = attRepository.findQuizRating(quizId);
			
			return new QuizRating(quiz, rating);
		} else {
			return null;
		}
	}

	@RequestMapping("/questions/{quizid}")
	public @ResponseBody List<Question> findQuestionsByQuizId(@PathVariable("quizid") Long quizId,
			Authentication auth) {
		List<Question> questions = questRepository.findQuestionsByQuizId(quizId);
		if (auth == null) {
			for (int i = 0; i < questions.size(); i++) {
				for (int j = 0; j < questions.get(i).getAnswers().size(); j++) {
					questions.get(i).getAnswers().get(j).setCorrect(false);
				}
			}
		} else {
			if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
				MyUser myUser = (MyUser) auth.getPrincipal();
				Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

				if (optUser.isPresent() && quizRepository.findById(quizId).isPresent()
						&& optUser.get().getId() == quizRepository.findById(quizId).get().getUser().getId()) {

					return questRepository.findQuestionsByQuizId(quizId);
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

	@RequestMapping(value = "/createquiz")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createQuizByAuth(Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent()) {
				User user = optUser.get();
				Quiz quiz = new Quiz(user, catRepository.findByName("Other").get(),
						difRepository.findByName("Easy").get());
				quizRepository.save(quiz);

				Question defaultQuestion1 = new Question(quiz);
				Question defaultQuestion2 = new Question(quiz);
				questRepository.save(defaultQuestion1);
				questRepository.save(defaultQuestion2);
				
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

	@RequestMapping(value = "/updatequiz", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> updateQuizByAuth(@RequestBody QuizUpdate quiz, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == quiz.getUser()) {
				if (catRepository.findById(quiz.getCategory()).isPresent()
						&& difRepository.findById(quiz.getDifficulty()).isPresent()
						&& quizRepository.findById(quiz.getQuizId()).isPresent()) {
					Category category = catRepository.findById(quiz.getCategory()).get();
					Difficulty difficulty = difRepository.findById(quiz.getDifficulty()).get();
					Quiz currentQuiz = quizRepository.findById(quiz.getQuizId()).get();

					currentQuiz.setCategory(category);
					currentQuiz.setDifficulty(difficulty);
					currentQuiz.setDescription(quiz.getDescription());
					currentQuiz.setMinutes(quiz.getMinutes());
					currentQuiz.setTitle(quiz.getTitle());
					quizRepository.save(currentQuiz);

					return new ResponseEntity<>("Quiz info was updated successfully", HttpStatus.OK);
				} else {
					return new ResponseEntity<>("There is no such category or difficulty or quiz",
							HttpStatus.INTERNAL_SERVER_ERROR); // 500;
				}
			} else {
				return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401
		}
	}

	@RequestMapping(value = "/savequestions/{quizid}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> saveQuestions(@PathVariable("quizid") Long quizId, @RequestBody List<Question> questions,
			Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && quizRepository.findById(quizId).isPresent()
					&& optUser.get().getId() == quizRepository.findById(quizId).get().getUser().getId()) {
				Question newQuestion;
				Answer newAnswer;
				Iterator<Question> iterator = questions.iterator();

				while (iterator.hasNext()) {
					Question question = iterator.next();
					if (question.getQuestionId() < 0) {
						newQuestion = new Question(question.getText(), question.getQuiz());
						questRepository.save(newQuestion);
						for (int i = 0; i < question.getAnswers().size(); i++) {
							newAnswer = new Answer(question.getAnswers().get(i).getText(), question.getAnswers().get(i).isCorrect(), newQuestion);
							answerRepository.save(newAnswer);
						}
					} else {
						for (int i = 0; i < question.getAnswers().size(); i++) {
							newAnswer = answerRepository.findById(question.getAnswers().get(i).getAnswerId()).get();
							
							newAnswer.setText(question.getAnswers().get(i).getText());
							newAnswer.setCorrect(question.getAnswers().get(i).isCorrect());
							answerRepository.save(newAnswer);
						}
						newQuestion = questRepository.findById(question.getQuestionId()).get();
						
						newQuestion.setText(question.getText());
						questRepository.save(newQuestion);
					}
				}
				
				return new ResponseEntity<>("Everything was saved successfully", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
			}
		} else {
			return new ResponseEntity<>("Authorization problems", HttpStatus.UNAUTHORIZED); // 401;
		}
	}

	@RequestMapping(value = "/deletequestion/{questionid}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteQuestionById(@PathVariable("questionid") Long questionId, Authentication auth) {
		Optional<Question> optQuestion = questRepository.findById(questionId);
		if (optQuestion.isPresent()) {
			if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
				MyUser myUser = (MyUser) auth.getPrincipal();
				Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());

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
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());
			Optional<Quiz> optQuiz = quizRepository.findById(quizId);
			
			if (optUser.isPresent() && optQuiz.isPresent() && optUser.get().getId() == optQuiz.get().getUser().getId()) {
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
	public ResponseEntity<?> sendAttempt(@RequestBody AttemptForm attemptForm, @PathVariable("quizid") Long quizId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = uRepository.findByUsername(myUser.getUsername());
			Optional<Quiz> optQuiz = quizRepository.findById(quizId);
			
			if (optUser.isPresent() && optQuiz.isPresent()) {
				Quiz quiz = optQuiz.get();
				User user = optUser.get();
				
				int score = 0;
				
				for (int i = 0; i < attemptForm.getAttemptAnswers().size(); i++) {
					if (answerRepository.findById(attemptForm.getAttemptAnswers().get(i).getAnswerId()).get().isCorrect()) score += 1;
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
}
