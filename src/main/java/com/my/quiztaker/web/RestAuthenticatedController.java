package com.my.quiztaker.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.PersonalInfo;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.CategoryRepository;
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
	private UserRepository urepository;

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
				Optional<User> optUser = urepository.findByUsername(myUser.getUsername());

				if (optUser.isPresent()
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

	@RequestMapping("/users/{userid}")
	@PreAuthorize("isAuthenticated()")
	public @ResponseBody PersonalInfo getLeaderboardAuth(@PathVariable("userid") Long userId, Authentication auth) {
		if (auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser")) {
			MyUser myUser = (MyUser) auth.getPrincipal();
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());
			if (optUser.isPresent() && optUser.get().getId() == userId) {
				UserPublic userPublic = urepository.findRatingByUserId(userId);

				// if user doesn't have any attempts
				if (userPublic == null) {
					return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), 0,
							attRepository.findAttemptsByUserId(userId), -1);
				}

				String username = optUser.get().getUsername();
				List<UserPublic> leaders = urepository.findLeaderBoard();
				int position = this.findPosition(username, leaders);

				return new PersonalInfo(optUser.get().getUsername(), optUser.get().getEmail(), userPublic.getRating(),
						attRepository.findAttemptsByUserId(userId), position);
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
			Optional<User> optUser = urepository.findByUsername(myUser.getUsername());

			if (optUser.isPresent() && optUser.get().getId() == userId) {
				List<Quiz> quizzesOfOthers = quizRepository.findPublishedQuizzesFromOtherUsers(userId);
				Double rating;
				QuizRatingQuestions quizRatingQuestions;
				Integer questions;

				List<QuizRatingQuestions> quizRatingsQuestionss = new ArrayList<QuizRatingQuestions>();

				for (Quiz quiz : quizzesOfOthers) {
					if (attRepository.findAttemptsForTheQuizByUserId(userId, quiz.getQuizId()) == 0) {
						rating = attRepository.findQuizRating(quiz.getQuizId());
						questions = questRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
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
