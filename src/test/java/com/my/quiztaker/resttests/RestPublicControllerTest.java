package com.my.quiztaker.resttests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.SignupCredentials;
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
public class RestPublicControllerTest {
	private static final String END_POINT_PATH = "";

	@Value("${spring.mail.username}")
	private String springMailUsername;

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
	public void resetRepos() throws Exception {
		this.deleteAll();
		this.checkAllRepos();
		User user = this.createCategoriesDifficultiesAndUsers();
		Quiz quiz = this.createQuiz(user);

		Question question1 = this.createDefaultQuestion(quiz);
		this.createDefaultAnswers(question1);
		Question question2 = this.createDefaultQuestion(quiz);
		this.createDefaultAnswers(question2);
	}

	@Test
	@Rollback
	public void testGetCategoriesAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/categories";

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));

		categoryRepository.deleteAll();

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
	}

	@Test
	@Rollback
	public void testGetDifficultiesAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/difficulties";

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3));

		difficultyRepository.deleteAll();

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
	}

	@Test
	@Rollback
	public void testGetQuizzesAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/quizzes";

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));

		User user1 = userRepository.findByUsername("user1").get();
		Quiz quiz1 = this.createQuiz(user1);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));

		quiz1.setStatus("Published");
		quizRepository.save(quiz1);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].rating").doesNotExist());

		User user2 = userRepository.findByUsername("user2").get();
		this.createAttempt(user2, quiz1);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].rating").value(5))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].questions").value(0));
	}

	@Test
	@Rollback
	public void testGetLeaderboardNoAuthCase() throws Exception {
		String requestURI = END_POINT_PATH + "/users";

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(0));

		User user1 = userRepository.findByUsername("user1").get();
		Quiz quiz1 = this.createQuiz(user1);
		User user2 = userRepository.findByUsername("user2").get();
		this.createAttempt(user2, quiz1);

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(1));

		this.createLeaderBoard(quiz1);
		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(10))
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(-1));
	}

	@Test
	@Rollback
	public void testGetQuizByIdAllCases() throws Exception {
		String requestURI = END_POINT_PATH + "/quizzes/";

		// Case when quiz is not found by id
		Long wrongId = Long.valueOf(11);
		String wrongRequestURI = requestURI + wrongId;
		mockMvc.perform(get(wrongRequestURI)).andExpect(status().isBadRequest());

		// Testing good case. Creating quiz first
		User user1 = userRepository.findByUsername("user1").get();
		Quiz quiz1 = this.createQuiz(user1);
		Long quiz1Id = quiz1.getQuizId();
		String goodRequestURI = requestURI + quiz1Id;

		mockMvc.perform(get(goodRequestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.quiz.difficulty.name").value("Easy"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.rating").doesNotExist());

		// Good case with rating;
		this.createLeaderBoard(quiz1);
		mockMvc.perform(get(goodRequestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.rating").value(5));
	}

	@Test
	@Rollback
	public void testGetQuestionsByQuizIdNoAuthWrongIdAndNoQuestionsCases() throws Exception {
		String requestURI = END_POINT_PATH + "/questions/";

		// Case when quiz is not found by id
		Long wrongId = Long.valueOf(11);
		String wrongRequestURI = requestURI + wrongId;
		mockMvc.perform(get(wrongRequestURI)).andExpect(status().isBadRequest());

		// Testing good case with no questions in quiz. Creating quiz first
		User user1 = userRepository.findByUsername("user1").get();
		Quiz quiz1 = this.createQuiz(user1);
		Long quiz1Id = quiz1.getQuizId();
		String goodRequestURI = requestURI + quiz1Id;

		mockMvc.perform(get(goodRequestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
	}

	@Test
	@Rollback
	public void testGetQuestionsByQuizIdNoAuthGoodCaseWithQuestions() throws Exception {
		String requestURI = END_POINT_PATH + "/questions/";

		// Testing good case with questions in quiz. Find quiz first
		Quiz quiz = ((List<Quiz>) quizRepository.findAll()).get(0);
		Long quizId = quiz.getQuizId();

		String goodRequestURI = requestURI + quizId;
		MvcResult result = mockMvc.perform(get(goodRequestURI)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2)).andReturn();
		String questionsAsString = result.getResponse().getContentAsString();

		TypeReference<List<Question>> typeReference = new TypeReference<List<Question>>() {
		};
		List<Question> questions = objectMapper.readValue(questionsAsString, typeReference);

		for (Question question : questions) {
			List<Answer> answers = question.getAnswers();
			for (Answer answer : answers) {
				assertThat(answer.isCorrect()).isFalse();
			}
		}
	}

	// Login functionality:
	@Test
	@Rollback
	public void testGetTokenBadCases() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		// Wrong username case:
		String wrongUsername = "user1Wrong";
		String goodPassword = "asas2233";

		AccountCredentials credentialsWrongUsername = new AccountCredentials(wrongUsername, goodPassword);

		String requestBodyWrongUsername = objectMapper.writeValueAsString(credentialsWrongUsername);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyWrongUsername))
				.andExpect(status().isUnauthorized()).andExpect(content().string("Bad credentials"));

		// Wrong email case:
		String wrongEmail = "user1@wrongmail.com";

		AccountCredentials credentialsWrongEmail = new AccountCredentials(wrongEmail, goodPassword);

		String requestBodyWrongEmail = objectMapper.writeValueAsString(credentialsWrongEmail);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyWrongEmail))
				.andExpect(status().isUnauthorized()).andExpect(content().string("Bad credentials"));

		// Wrong password case:
		String goodUsername = "user1";
		String wrongPassword = "wrong";

		AccountCredentials credentialsWrongPassword = new AccountCredentials(goodUsername, wrongPassword);

		String requestBodyWrongPassword = objectMapper.writeValueAsString(credentialsWrongPassword);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyWrongPassword))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetTokenUnverifiedByUsernameCase() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		String unverifiedUsername = "user3";
		String unverifiedEmail = "user3@mail.com";
		String unverifiedPassword = "asas2233";

		this.createCustomUser(unverifiedUsername, unverifiedEmail, false);

		// Unverified by username case:
		AccountCredentials credentialsUnverifiedByUsername = new AccountCredentials(unverifiedUsername,
				unverifiedPassword);

		String requestBodyUnverifiedByUsername = objectMapper.writeValueAsString(credentialsUnverifiedByUsername);

		if (this.springMailUsername.equals("default_value")) {
			mockMvc.perform(
					post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverifiedByUsername))
					.andExpect(status().isCreated());
		} else {
			mockMvc.perform(
					post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverifiedByUsername))
					.andExpect(status().isAccepted()).andExpect(header().exists("Host"));
		}
	}

	@Test
	@Rollback
	public void testGetTokenUnverifiedByEmailCase() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		String unverifiedUsername = "user3";
		String unverifiedEmail = "user3@mail.com";
		String unverifiedPassword = "asas2233";

		this.createCustomUser(unverifiedUsername, unverifiedEmail, false);

		// Unverified by email case:
		AccountCredentials credentialsUnverifiedByEmail = new AccountCredentials(unverifiedEmail, unverifiedPassword);

		String requestBodyUnverifiedByEmail = objectMapper.writeValueAsString(credentialsUnverifiedByEmail);

		if (this.springMailUsername.equals("default_value")) {
			mockMvc.perform(
					post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverifiedByEmail))
					.andExpect(status().isCreated());
		} else {
			mockMvc.perform(
					post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUnverifiedByEmail))
					.andExpect(status().isAccepted()).andExpect(header().exists("Host"));
		}
	}

	@Test
	@Rollback
	public void testGetTokenGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/login";

		String goodUsername = "user1";
		String goodPassword = "asas2233";

		AccountCredentials credentialsGood = new AccountCredentials(goodUsername, goodPassword);

		String requestBodyGood = objectMapper.writeValueAsString(credentialsGood);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
				.andExpect(status().isOk()).andExpect(header().exists("Authorization"))
				.andExpect(header().string("Authorization", Matchers.containsString("Bearer")))
				.andExpect(header().exists("Host")).andExpect(header().exists("Allow"))
				.andExpect(header().string("Allow", Matchers.equalTo("USER")));
	}

	// Signup functionality:

	@Test
	@Rollback
	public void testSignUpGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/signup";

		String goodUsername = "user3";
		String goodPassword = "asas2233";
		String goodEmail = "user3@mail.com";

		SignupCredentials credentialsGood = new SignupCredentials(goodUsername, goodEmail, goodPassword);

		String requestBodyGood = objectMapper.writeValueAsString(credentialsGood);

		if (this.springMailUsername.equals("default_value")) {
			mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
					.andExpect(status().isCreated()).andExpect(header().exists("Host"));
		} else {
			mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyGood))
					.andExpect(status().isOk()).andExpect(header().exists("Host"));
		}
	}

	@Test
	@Rollback
	public void testSignUpBadCases() throws Exception {
		String requestURI = END_POINT_PATH + "/signup";

		// Username in use case:
		String usernameInUse = "user1";
		String goodPassword = "asas2233";
		String goodEmail = "user3@mail.com";

		SignupCredentials credentialsUsernameInUse = new SignupCredentials(usernameInUse, goodEmail, goodPassword);

		String requestBodyUsernameInUse = objectMapper.writeValueAsString(credentialsUsernameInUse);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyUsernameInUse))
				.andExpect(status().isConflict());

		// Email in use case:
		String emailInUse = "user1@mail.com";
		String goodUsername = "user3";
		SignupCredentials credentialsEmailInUse = new SignupCredentials(goodUsername, emailInUse, goodPassword);

		String requestBodyEmailInUse = objectMapper.writeValueAsString(credentialsEmailInUse);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBodyEmailInUse))
				.andExpect(status().isNotAcceptable());
	}

	// Test verify functionality
	@Test
	@Rollback
	public void testVerifyUserWrongIdOrUserAlreadyVerifiedCases() throws Exception {
		String requestURI = END_POINT_PATH + "/verify/";

		String verificationCode = "some12";

		// Wrong user id case:
		String worngIdRequestURI = requestURI + Long.valueOf(12);

		mockMvc.perform(post(worngIdRequestURI).contentType(MediaType.APPLICATION_JSON).content(verificationCode))
				.andExpect(status().isBadRequest()).andExpect(content().string("Wrong user id"));

		// User is already verified case:
		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String allreadyVerifiedRequestURI = requestURI + user1Id;
		mockMvc.perform(
				post(allreadyVerifiedRequestURI).contentType(MediaType.APPLICATION_JSON).content(verificationCode))
				.andExpect(status().isBadRequest()).andExpect(content().string("The user is already verified"));
	}

	@Test
	@Rollback
	public void testVerifyUserCodeIsIncorrectCase() throws Exception {
		String requestURI = END_POINT_PATH + "/verify/";

		String verificationCode = "some12";

		String unverifiedUsername = "user3";
		String unverifiedEmail = "user3@mail.com";

		User unverifiedUser = this.createCustomUser(unverifiedUsername, unverifiedEmail, false);
		Long unverifiedUserId = unverifiedUser.getId();

		String rightIdRequestURI = requestURI + unverifiedUserId;

		mockMvc.perform(post(rightIdRequestURI).contentType(MediaType.APPLICATION_JSON).content(verificationCode))
				.andExpect(status().isConflict()).andExpect(content().string("Verification code is incorrect"));
	}

	@Test
	@Rollback
	public void testVerifyUserGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/verify/";

		String verificationCode = "123453";

		String unverifiedUsername = "user3";
		String unverifiedEmail = "user3@mail.com";

		User unverifiedUser = this.createCustomUser(unverifiedUsername, unverifiedEmail, false);
		Long unverifiedUserId = unverifiedUser.getId();

		String rightIdRequestURI = requestURI + unverifiedUserId;

		mockMvc.perform(post(rightIdRequestURI).contentType(MediaType.APPLICATION_JSON).content(verificationCode))
				.andExpect(status().isOk());
	}

	@Test
	@Rollback
	public void testResetPasswordNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/resetpassword";

		String emailNotFoundCase = "wrong@mail.com";

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(emailNotFoundCase))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Rollback
	public void testResetPasswordNotVerifiedCase() throws Exception {
		String requestURI = END_POINT_PATH + "/resetpassword";

		String unverifiedUsername = "user3";
		String unverifiedEmail = "user3@mail.com";

		this.createCustomUser(unverifiedUsername, unverifiedEmail, false);

		mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(unverifiedEmail))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("User with this email (" + unverifiedEmail + ") is not verified"));
	}

	@Test
	@Rollback
	public void testResetPasswordGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/resetpassword";

		String goodEmail = "user1@mail.com";

		if (this.springMailUsername.equals("default_value")) {
			mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(goodEmail))
					.andExpect(status().isInternalServerError())
					.andExpect(content().string("This service isn't available at the moment"));
		} else {
			mockMvc.perform(post(requestURI).contentType(MediaType.APPLICATION_JSON).content(goodEmail))
					.andExpect(status().isOk());
		}

	}

	private void deleteAll() {
		answerRepository.deleteAll();
		questionRepository.deleteAll();
		quizRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		attemptRepository.deleteAll();
		userRepository.deleteAll();
	}

	private void checkAllRepos() {
		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).isEmpty();

		List<Question> questions = (List<Question>) questionRepository.findAll();
		assertThat(questions).isEmpty();

		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).isEmpty();

		List<Category> categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).isEmpty();

		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).isEmpty();

		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		assertThat(attempts).isEmpty();

		List<User> users = (List<User>) userRepository.findAll();
		assertThat(users).isEmpty();
	}

	private User createCategoriesDifficultiesAndUsers() {
		this.createDifficulties();

		this.createCategories();

		User user1 = this.createUsers();

		return user1;
	}

	private void createDifficulties() {
		Difficulty hardDifficulty = new Difficulty("Hard", 3);
		Difficulty mediumDifficulty = new Difficulty("Medium", 2);
		Difficulty smallDifficulty = new Difficulty("Easy", 1);
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

	private User createUsers() {
		User user1 = new User("user1", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				"user1@mail.com", null, true);
		userRepository.save(user1);

		User user2 = new User("user2", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				"user2@mail.com", null, true);
		userRepository.save(user2);

		return user1;
	}

	private Quiz createQuiz(User user) {
		Category otherCategory = categoryRepository.findByName("Other").get();
		Difficulty easyDifficulty = difficultyRepository.findByName("Easy").get();

		Quiz newQuiz = new Quiz(user, otherCategory, easyDifficulty);
		quizRepository.save(newQuiz);

		return newQuiz;
	}

	private Attempt createAttempt(User user, Quiz quiz) {
		Attempt attempt = new Attempt(5, quiz, user, 5);
		attemptRepository.save(attempt);

		return attempt;
	}

	private Attempt createCustomAttempt(User user, Quiz quiz, int score) {
		Attempt attempt = new Attempt(score, quiz, user, 5);
		attemptRepository.save(attempt);

		return attempt;
	}

	private void createLeaderBoard(Quiz quiz) {
		String username = "user";
		String email = "mail@test.com";
		User currentUser;

		for (int i = 3; i < 13; i++) {
			currentUser = this.createCustomUser(username + String.valueOf(i), String.valueOf(i) + email, true);
			this.createCustomAttempt(currentUser, quiz, i);
		}
	}

	private User createCustomUser(String username, String email, boolean verified) {
		int usernameLength = username.length();
		String verificationCode = "12345" + username.substring(usernameLength - 1, usernameLength);
		if (verified) {
			verificationCode = null;
		}
		User customUser = new User(username, "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER",
				email, verificationCode, verified);
		userRepository.save(customUser);

		return customUser;
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
