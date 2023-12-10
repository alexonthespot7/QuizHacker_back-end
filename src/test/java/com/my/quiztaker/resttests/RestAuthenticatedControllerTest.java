package com.my.quiztaker.resttests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.quiztaker.forms.AccountCredentials;
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

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class RestAuthenticatedControllerTest {
	private static final String END_POINT_PATH = "";

	private String jwtToken;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeAll
	public void resetReposAndGetToken() throws Exception {
		this.resetReposAndAddData();
		this.getToken();
	}

	private void getToken() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		String goodUsername = "user1";
		String goodPassword = "asas2233";
		AccountCredentials credentialsGood = new AccountCredentials(goodUsername, goodPassword);
		String requestBodyGood = objectMapper.writeValueAsString(credentialsGood);

		// Perform the login request and retrieve the token
		MvcResult result = mockMvc
				.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
				.andExpect(status().isOk()).andReturn();

		// Retrieve the JWT token from the login response
		jwtToken = result.getResponse().getHeader("Authorization");
	}

	private void resetReposAndAddData() {
		this.deleteAll();
		this.addData();
	}

	private void deleteAll() {
		attemptRepository.deleteAll();
		quizRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		userRepository.deleteAll();
		answerRepository.deleteAll();
		questionRepository.deleteAll();
	}

	private void addData() {
		this.createDifficulties(); // Hard, medium, easy
		this.createCategories(); // IT and Other
		this.createUsers(); // user0, user1, user2

		this.createTwoPublishedQuizzes();
		this.createTwoCreatedQuizzes();

		// creating two default questions and four default answers for each quzi:
		this.createDefaultQuestionsAndAnswers();

		// creating one attempt for each quiz with user0;
		this.createAttempts();
	}

	private void createDifficulties() {
		Difficulty hardDifficulty = new Difficulty("Hard", 1.0);
		Difficulty mediumDifficulty = new Difficulty("Medium", 0.66);
		Difficulty smallDifficulty = new Difficulty("Easy", 0.33);
		difficultyRepository.save(hardDifficulty);
		difficultyRepository.save(mediumDifficulty);
		difficultyRepository.save(smallDifficulty);
	}

	private void createCategories() {
		Category otherCategory = new Category("Other");
		Category itCategory = new Category("IT");
		categoryRepository.save(otherCategory);
		categoryRepository.save(itCategory);
	}

	private void createUsers() {
		User user0 = new User("user0", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				"user0@mail.com", null, true);
		userRepository.save(user0);

		User user1 = new User("user1", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				"user1@mail.com", null, true);
		userRepository.save(user1);

		User user2 = new User("user2", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				"user2@mail.com", null, true);
		userRepository.save(user2);
	}

	// Creating two published quizzes for user 1 and user 2 (other and easy)
	private void createTwoPublishedQuizzes() {
		Category otherCategory = categoryRepository.findByName("Other").get();
		Difficulty easyDifficulty = difficultyRepository.findByName("Easy").get();

		User user1 = userRepository.findByUsername("user1").get();
		User user2 = userRepository.findByUsername("user2").get();

		Quiz newPublishedQuizUser1 = new Quiz("Published quiz 1", "my_desc1", otherCategory, easyDifficulty, user1, 5,
				"Published");
		Quiz newPublishedQuizUser2 = new Quiz("Published quiz 2", "my_desc2", otherCategory, easyDifficulty, user2, 3,
				"Published");
		quizRepository.save(newPublishedQuizUser1);
		quizRepository.save(newPublishedQuizUser2);
	}

	// Creating two published quizzes with it category: one user1 hard, one user2
	// medium
	private void createTwoCreatedQuizzes() {
		Category itCategory = categoryRepository.findByName("IT").get();
		Difficulty hardDifficulty = difficultyRepository.findByName("Hard").get();
		Difficulty mediumDifficulty = difficultyRepository.findByName("Medium").get();

		User user1 = userRepository.findByUsername("user1").get();
		User user2 = userRepository.findByUsername("user2").get();

		Quiz quizCategoryITDifficultyHardUser1 = new Quiz(user1, itCategory, hardDifficulty);
		Quiz quizCategoryITDifficultyMediumUser2 = new Quiz(user2, itCategory, mediumDifficulty);
		quizRepository.save(quizCategoryITDifficultyHardUser1);
		quizRepository.save(quizCategoryITDifficultyMediumUser2);
	}

	private void createDefaultQuestionsAndAnswers() {
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();

		for (Quiz quiz : quizzes) {
			this.createDefaultQuestionsAndAnswers(quiz);
		}
	}

	private void createDefaultQuestionsAndAnswers(Quiz quiz) {
		Question question1 = this.createDefaultQuestion(quiz);
		this.createDefaultAnswers(question1);
		Question question2 = this.createDefaultQuestion(quiz);
		this.createDefaultAnswers(question2);
	}

	private void createAttempts() {
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		User user0 = userRepository.findByUsername("user0").get();

		for (Quiz quiz : quizzes) {
			this.createAttempt(user0, quiz);
		}
	}

	private Attempt createAttempt(User user, Quiz quiz) {
		Attempt attempt = new Attempt(5, quiz, user, 5);
		attemptRepository.save(attempt);

		return attempt;
	}

	private Question createDefaultQuestion(Quiz quiz) {
		Question newQuestion = new Question(quiz);
		questionRepository.save(newQuestion);

		return newQuestion;
	}

	private void createDefaultAnswers(Question question) {
		Answer defaultAnswer1 = new Answer(question, true);
		Answer defaultAnswer2 = new Answer(question, false);
		Answer defaultAnswer3 = new Answer(question, false);
		Answer defaultAnswer4 = new Answer(question, false);

		answerRepository.save(defaultAnswer1);
		answerRepository.save(defaultAnswer2);
		answerRepository.save(defaultAnswer3);
		answerRepository.save(defaultAnswer4);
	}
}
