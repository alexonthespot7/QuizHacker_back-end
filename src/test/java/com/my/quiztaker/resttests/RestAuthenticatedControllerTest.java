package com.my.quiztaker.resttests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
import com.my.quiztaker.forms.AttemptAnswer;
import com.my.quiztaker.forms.AttemptForm;
import com.my.quiztaker.forms.QuizUpdate;
import com.my.quiztaker.forms.SignupCredentials;
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

	@Test
	@Rollback
	public void testQuestionsByQuizAuthenticatedOtherUserQuizGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/questions/";

		// Case when fetching quiz of the other user:
		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzes = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		Quiz quizNotFromUser1 = quizzes.get(0);
		Long quizNotFromUser1Id = quizNotFromUser1.getQuizId();
		String notUser1QuizURI = requestURI + quizNotFromUser1Id;

		MvcResult result = mockMvc.perform(get(notUser1QuizURI).header("Authorization", jwtToken))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2)).andReturn();
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

	@Test
	@Rollback
	public void testQuestionsByQuizAuthenticatedThisUserQuizGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/questions/";

		// Case when fetching quiz of the other user:
		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		// Looking for the quiz created by user1
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		Quiz user1Quiz = null;
		for (Quiz quiz : quizzes) {
			if (quiz.getUser().getId() == user1Id) {
				user1Quiz = quiz;
				break;
			}
		}
		Long user1QuizId = user1Quiz.getQuizId();
		String user1QuizURI = requestURI + user1QuizId;

		MvcResult result = mockMvc.perform(get(user1QuizURI).header("Authorization", jwtToken))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2)).andReturn();
		String questionsAsString = result.getResponse().getContentAsString();

		TypeReference<List<Question>> typeReference = new TypeReference<List<Question>>() {
		};
		List<Question> questions = objectMapper.readValue(questionsAsString, typeReference);

		boolean allFalse;

		// Checking that not all the answers are false, so that the answers are shown to
		// the quiz owner
		for (Question question : questions) {
			allFalse = true;
			List<Answer> answers = question.getAnswers();
			for (Answer answer : answers) {
				if (answer.isCorrect()) {
					allFalse = false;
					break;
				}
			}
			assertThat(allFalse).isFalse();
		}
	}

	@Test
	@Rollback
	public void testGetPersonalInfoIdMissmatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/users/";

		// Userid missmatch case (the authentication is for user1, let's use user's 2
		// id):
		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();

		String requestURIWrongId = requestURI + user2Id;

		mockMvc.perform(get(requestURIWrongId).header("Authorization", jwtToken)).andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetPersonalInfoNoRatingCase() throws Exception {
		String requestURI = END_POINT_PATH + "/users/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String requestURINoRating = requestURI + user1Id;

		mockMvc.perform(get(requestURINoRating).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(-1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.attempts").value(0))
				.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("user1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.score").value(0));
	}

	@Test
	@Rollback
	public void testGetPersonalInfoWithRatingCase() throws Exception {
		String requestURI = END_POINT_PATH + "/users/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();
		String requestURINoRating = requestURI + user1Id;

		List<Quiz> quizzesForUser1 = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		Quiz quizForUser1 = quizzesForUser1.get(0);
		this.createAttempt(user1, quizForUser1);

		mockMvc.perform(get(requestURINoRating).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.attempts").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("user1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.score").value(5 * 1));
	}

	@Test
	@Rollback
	public void testGetLeaderboardAuthWithRatingCases() throws Exception {
		String requestURI = END_POINT_PATH + "/usersauth/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String requestURINoRating = requestURI + user1Id;

		List<Quiz> quizzesForUser1 = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		Quiz quizForUser1 = quizzesForUser1.get(0);
		this.createAttempt(user1, quizForUser1);

		mockMvc.perform(get(requestURINoRating).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(2));

		// Getting more points to get to the 1 position;
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);
		this.createAttempt(user1, quizForUser1);

		mockMvc.perform(get(requestURINoRating).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(1));
	}

	@Test
	@Rollback
	public void testGetLeaderboardAuthNoRatingCase() throws Exception {
		String requestURI = END_POINT_PATH + "/usersauth/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String requestURINoRating = requestURI + user1Id;

		mockMvc.perform(get(requestURINoRating).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.users.size()").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.position").value(-1));
	}

	@Test
	@Rollback
	public void testGetLeaderboardAuthWronIdCases() throws Exception {
		String requestURI = END_POINT_PATH + "/usersauth/";

		// Case user's id is not in the db
		String requestURIWrongId = requestURI + Long.valueOf(20);

		mockMvc.perform(get(requestURIWrongId).header("Authorization", jwtToken)).andExpect(status().isUnauthorized());

		// Case other user's id:
		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();
		String requestURIOtherUserId = requestURI + user2Id;

		mockMvc.perform(get(requestURIOtherUserId).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetQuizzesOfOthersAuthIdMissmatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/quizzesbyuser/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();
		String requestURIIdMissmatch = requestURI + user2Id;

		mockMvc.perform(get(requestURIIdMissmatch).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetQuizzesOfOthersAuthGoodCases() throws Exception {
		String requestURI = END_POINT_PATH + "/quizzesbyuser/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();
		String requestURIGood = requestURI + user1Id;

		mockMvc.perform(get(requestURIGood).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].rating").value(5));

		List<Quiz> quizzesOfOthers = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		Quiz quizForUser1 = quizzesOfOthers.get(0);

		this.createAttempt(user1, quizForUser1);

		mockMvc.perform(get(requestURIGood).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
	}

	@Test
	@Rollback
	public void testGetPersonalQuizzesWrongIdCase() throws Exception {
		String requestURI = END_POINT_PATH + "/personalquizzes/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();
		String requestURIWrongId = requestURI + user2Id;

		mockMvc.perform(get(requestURIWrongId).header("Authorization", jwtToken)).andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetPersonalQuizzesGoodCases() throws Exception {
		String requestURI = END_POINT_PATH + "/personalquizzes/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();
		String requestURIGood = requestURI + user1Id;

		mockMvc.perform(get(requestURIGood).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].rating").value(5));

		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		for (Quiz quiz : quizzes) {
			quiz.setStatus("Published");
			quizRepository.save(quiz);
		}

		mockMvc.perform(get(requestURIGood).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].rating").value(5));
	}

	@Test
	@Rollback
	public void testCreateQuizByAuthGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/createquiz";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		mockMvc.perform(post(requestURI).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andExpect(header().exists("Host"));

		// Initially two quizzes was created for user1.
		List<Quiz> quizzes = quizRepository.findQuizzesByUserId(user1Id);
		assertThat(quizzes).hasSize(3);
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthQuizNotInDBCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizToUpdate = quizzesUser1.get(0);

		QuizUpdate quizUpdateQuizNotInDB = this.createQuizUpdateInstance(quizToUpdate, user1Id);

		String requestBodyQuizIsNotInDB = objectMapper.writeValueAsString(quizUpdateQuizNotInDB);
		String requestURIQuizNotInDB = requestURI + Long.valueOf(20);
		MvcResult result = mockMvc
				.perform(put(requestURIQuizNotInDB).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyQuizIsNotInDB))
				.andExpect(status().isBadRequest()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("There's no quiz with this id");
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthIdMissmatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizToUpdate = quizzesUser1.get(0);
		Quiz wrongQuiz = quizzesUser1.get(1);
		Long wrongQuizId = wrongQuiz.getQuizId();

		QuizUpdate quizUpdateIdMissmatch = this.createQuizUpdateInstance(quizToUpdate, user1Id);

		String requestBodyIdMissmatch = objectMapper.writeValueAsString(quizUpdateIdMissmatch);
		String requestURIIdMissmatch = requestURI + wrongQuizId;
		mockMvc.perform(put(requestURIIdMissmatch).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyIdMissmatch))
				.andExpect(status().isBadRequest()).andExpect(content().string(
						"The id missmatch: provided in the path id doesn't equal the id of the quiz in request body"));
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthTryingToUpdateOtherUserQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();

		List<Quiz> quizzesUser2 = quizRepository.findQuizzesByUserId(user2Id);
		Quiz quizOfUser2ToUpdate = quizzesUser2.get(0);
		Long quizOfUser2ToUpdateId = quizOfUser2ToUpdate.getQuizId();

		QuizUpdate quizUpdateOtherUserQuiz = this.createQuizUpdateInstance(quizOfUser2ToUpdate, user2Id);

		String requestBodyOtherUserQuiz = objectMapper.writeValueAsString(quizUpdateOtherUserQuiz);
		String requestURIOtherUserQuiz = requestURI + quizOfUser2ToUpdateId;
		MvcResult result = mockMvc
				.perform(put(requestURIOtherUserQuiz).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyOtherUserQuiz))
				.andExpect(status().isUnauthorized()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("You are not allowed to get someone else's info");
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthCategoryNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizToUpdate = quizzesUser1.get(0);
		Long quizToUpdateId = quizToUpdate.getQuizId();

		QuizUpdate quizUpdateCategoryNotFound = this.createQuizUpdateInstance(quizToUpdate, user1Id);
		quizUpdateCategoryNotFound.setCategory(Long.valueOf(20));

		String requestBodyCategoryNotFound = objectMapper.writeValueAsString(quizUpdateCategoryNotFound);
		String requestURICategoryNotFound = requestURI + quizToUpdateId;
		MvcResult result = mockMvc
				.perform(put(requestURICategoryNotFound).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyCategoryNotFound))
				.andExpect(status().isBadRequest()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("Category can't be found by ID");
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthDifficultyNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizToUpdate = quizzesUser1.get(0);
		Long quizToUpdateId = quizToUpdate.getQuizId();

		QuizUpdate quizUpdateDifficultyNotFound = this.createQuizUpdateInstance(quizToUpdate, user1Id);
		quizUpdateDifficultyNotFound.setDifficulty(Long.valueOf(20));

		String requestBodyDifficultyNotFound = objectMapper.writeValueAsString(quizUpdateDifficultyNotFound);
		String requestURIDifficultyNotFound = requestURI + quizToUpdateId;
		MvcResult result = mockMvc
				.perform(put(requestURIDifficultyNotFound).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyDifficultyNotFound))
				.andExpect(status().isBadRequest()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("Difficulty can't be found by ID");
	}

	@Test
	@Rollback
	public void testUpdateQuizByAuthGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updatequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizToUpdate = quizzesUser1.get(0);
		Long quizToUpdateId = quizToUpdate.getQuizId();

		String status = quizToUpdate.getStatus();
		Double rating = attemptRepository.findQuizRating(quizToUpdateId);

		String updatedTitle = "My title";
		String updatedDescription = "My description";
		Integer updatedMinutes = 2;
		Category itCategory = categoryRepository.findByName("IT").get();
		Long itCategoryId = itCategory.getCategoryId();

		Difficulty easyDifficulty = difficultyRepository.findByName("Easy").get();
		Long easyDifficultyId = easyDifficulty.getDifficultyId();

		QuizUpdate quizUpdateInstance = new QuizUpdate(quizToUpdateId, updatedTitle, updatedDescription,
				easyDifficultyId, updatedMinutes, rating, status, user1Id, itCategoryId);

		String requestBody = objectMapper.writeValueAsString(quizUpdateInstance);
		String requestURIGood = requestURI + quizToUpdateId;
		mockMvc.perform(put(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());

		Optional<Quiz> optionalUpdatedQuiz = quizRepository.findById(quizToUpdateId);
		assertThat(optionalUpdatedQuiz).isPresent();

		Quiz updatedQuiz = optionalUpdatedQuiz.get();
		assertThat(updatedQuiz.getTitle()).isEqualTo(updatedTitle);
		assertThat(updatedQuiz.getDescription()).isEqualTo(updatedDescription);
		assertThat(updatedQuiz.getMinutes()).isEqualTo(updatedMinutes);
		assertThat(updatedQuiz.getDifficulty()).isEqualTo(easyDifficulty);
		assertThat(updatedQuiz.getStatus()).isEqualTo(status);
		assertThat(updatedQuiz.getQuestions()).isEqualTo(quizToUpdate.getQuestions());
		assertThat(updatedQuiz.getUser()).isEqualTo(user1);
	}

	@Test
	@Rollback
	public void testSaveQuestionsQuizNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/savequestions/";

		List<Question> questionsQuizNotFound = new ArrayList<Question>();

		String requestBodyQuizNotFound = objectMapper.writeValueAsString(questionsQuizNotFound);
		String requestURIQuizNotFound = requestURI + Long.valueOf(20);
		MvcResult result = mockMvc
				.perform(put(requestURIQuizNotFound).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyQuizNotFound))
				.andExpect(status().isBadRequest()).andReturn();
		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("There's no quiz with this id");
	}

	@Test
	@Rollback
	public void testSaveQuestionsTryingToChangeOtherUserQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/savequestions/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();

		List<Quiz> quizzesUser2 = quizRepository.findQuizzesByUserId(user2Id);
		Quiz quizOfUser2ToUpdate = quizzesUser2.get(0);
		Long quizOfUser2ToUpdateId = quizOfUser2ToUpdate.getQuizId();

		List<Question> questionsTryingToChangeOtherUserQuiz = new ArrayList<Question>();

		String requestBodyTryingToChangeOtherUserQuiz = objectMapper
				.writeValueAsString(questionsTryingToChangeOtherUserQuiz);
		String requestURITryingToChangeOtherUserQuiz = requestURI + quizOfUser2ToUpdateId;
		MvcResult result = mockMvc
				.perform(put(requestURITryingToChangeOtherUserQuiz).header("Authorization", jwtToken)
						.contentType(MediaType.APPLICATION_JSON).content(requestBodyTryingToChangeOtherUserQuiz))
				.andExpect(status().isUnauthorized()).andReturn();
		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("You are not allowed to get someone else's info");
	}

	@Test
	@Rollback
	public void testSaveQuestionsGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/savequestions/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		List<Quiz> quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizOfUser1ToUpdate = quizzesUser1.get(0);
		Long quizOfUser1ToUpdateId = quizOfUser1ToUpdate.getQuizId();

		String requestBody = "[\r\n" + "    {\r\n" + "        \"questionId\":1,\r\n"
				+ "        \"text\":\"Custom question\",\r\n"
				+ "        \"quiz\":{\"quizId\":1,\"title\":\"Published quiz 1\"},\r\n"
				+ "        \"answers\":[{\"answerId\":1,\"text\":\"Custom answer\",\"correct\":true},{\"answerId\":2,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":3,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":4,\"text\":\"Custom answer\",\"correct\":false}]\r\n"
				+ "    },\r\n" + "    {\r\n" + "        \"questionId\":2,\r\n"
				+ "        \"text\":\"Custom question\",\r\n"
				+ "        \"quiz\":{\"quizId\":1,\"title\":\"Published quiz 1\"},\r\n"
				+ "        \"answers\":[{\"answerId\":5,\"text\":\"Custom answer\",\"correct\":true},{\"answerId\":6,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":7,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":8,\"text\":\"Custom answer\",\"correct\":false}]\r\n"
				+ "    },\r\n" + "    {\r\n" + "        \"questionId\":-3,\r\n"
				+ "        \"text\":\"Custom question\",\r\n"
				+ "        \"quiz\":{\"quizId\":1,\"title\":\"Published quiz 1\"},\r\n"
				+ "        \"answers\":[{\"answerId\":-1,\"text\":\"Custom answer\",\"correct\":true},{\"answerId\":-2,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":-3,\"text\":\"Custom answer\",\"correct\":false},{\"answerId\":-4,\"text\":\"Custom answer\",\"correct\":false}]\r\n"
				+ "    }\r\n" + "]";

		String requestURIGood = requestURI + quizOfUser1ToUpdateId;
		mockMvc.perform(put(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());

		quizzesUser1 = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quizOfUser1Updated = quizzesUser1.get(0);

		assertThat(quizOfUser1Updated.getQuestions()).hasSize(3);
		assertThat(quizOfUser1Updated.getQuestions().get(0).getText()).isEqualTo("Custom question");
	}

	@Test
	@Rollback
	public void testDeleteQuestionByIdNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequestion/";

		String requestURINotFound = requestURI + Long.valueOf(100);
		MvcResult result = mockMvc.perform(delete(requestURINotFound).header("Authorization", jwtToken))
				.andExpect(status().isBadRequest()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("One or more of your questions that supposed to be in db can't be found in db");
	}

	@Test
	@Rollback
	public void testDeleteQuestionByIdOtherUserQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequestion/";

		User user2 = userRepository.findByUsername("user2").get();
		List<Quiz> quizzesOfUser2 = user2.getQuizzes();
		Quiz quiz1OfUser2 = quizzesOfUser2.get(0);
		List<Question> questionsOfQuiz1OfUser2 = quiz1OfUser2.getQuestions();
		Question question1OfQuiz1OfUser2 = questionsOfQuiz1OfUser2.get(0);
		Long question1OfQuiz1OfUser2Id = question1OfQuiz1OfUser2.getQuestionId();

		String requestURIOtherUserQuiz = requestURI + question1OfQuiz1OfUser2Id;
		MvcResult result = mockMvc.perform(delete(requestURIOtherUserQuiz).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("You are not allowed to get someone else's info");
	}

	@Test
	@Rollback
	public void testDeleteQuestionByIdGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequestion/";

		User user1 = userRepository.findByUsername("user1").get();
		List<Quiz> quizzesOfUser1 = user1.getQuizzes();
		Quiz quiz1OfUser1 = quizzesOfUser1.get(0);
		List<Question> questionsOfQuiz1OfUser1 = quiz1OfUser1.getQuestions();
		Question question1OfQuiz1OfUser1 = questionsOfQuiz1OfUser1.get(0);
		Long question1OfQuiz1OfUser1Id = question1OfQuiz1OfUser1.getQuestionId();

		String requestURINotFound = requestURI + question1OfQuiz1OfUser1Id;
		mockMvc.perform(delete(requestURINotFound).header("Authorization", jwtToken)).andExpect(status().isOk());

		Optional<Question> optionalQuestionNull = questionRepository.findById(question1OfQuiz1OfUser1Id);
		assertThat(optionalQuestionNull).isNotPresent();
	}

	@Test
	@Rollback
	public void testPublishQuizNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/publishquiz/";

		String requestURINotFound = requestURI + Long.valueOf(100);
		MvcResult result = mockMvc.perform(post(requestURINotFound).header("Authorization", jwtToken))
				.andExpect(status().isBadRequest()).andReturn();
		
		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("There's no quiz with this id");
	}

	@Test
	@Rollback
	public void testPublishQuizOtherUserQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/publishquiz/";

		User user2 = userRepository.findByUsername("user2").get();
		List<Quiz> quizzesOfUser2 = user2.getQuizzes();
		Quiz quiz1OfUser2 = quizzesOfUser2.get(0);
		Long quiz1OfUser2Id = quiz1OfUser2.getQuizId();

		String requestURIOtherUserQuiz = requestURI + quiz1OfUser2Id;
		MvcResult result = mockMvc.perform(post(requestURIOtherUserQuiz).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized()).andReturn();

		String message = result.getResponse().getErrorMessage();
		assertThat(message).isEqualTo("You are not allowed to get someone else's info");
	}

	@Test
	@Rollback
	public void testPublishQuizGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/publishquiz/";

		User user1 = userRepository.findByUsername("user1").get();
		List<Quiz> quizzesOfUser1 = user1.getQuizzes();

		Quiz createdQuizOfUser1 = null;

		for (Quiz quiz : quizzesOfUser1) {
			if (quiz.getStatus().equals("Created")) {
				createdQuizOfUser1 = quiz;
				break;
			}
		}

		Long createdQuizOfUser1Id = createdQuizOfUser1.getQuizId();

		String requestURINotFound = requestURI + createdQuizOfUser1Id;
		mockMvc.perform(post(requestURINotFound).header("Authorization", jwtToken)).andExpect(status().isOk());

		Optional<Quiz> optionalPublishedQuiz = quizRepository.findById(createdQuizOfUser1Id);
		assertThat(optionalPublishedQuiz).isPresent();

		Quiz publishedQuiz = optionalPublishedQuiz.get();
		assertThat(publishedQuiz.getStatus()).isEqualTo("Published");
	}

	@Test
	@Rollback
	public void testSendAttemptQuizNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		AttemptForm attemptFormQuizNotFound = new AttemptForm();
		String requestBodyQuizNotFound = objectMapper.writeValueAsString(attemptFormQuizNotFound);
		String requestURINotFound = requestURI + Long.valueOf(100);
		mockMvc.perform(post(requestURINotFound).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyQuizNotFound))
				.andExpect(status().isBadRequest()).andExpect(content().string("Quiz was not found for provided ID"));
	}

	@Test
	@Rollback
	public void testSendAttemptOwnQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user1 = userRepository.findByUsername("user1").get();
		Quiz quiz1User1 = user1.getQuizzes().get(0);
		Long quiz1User1Id = quiz1User1.getQuizId();

		AttemptForm attemptFormOwnQuiz = new AttemptForm();
		String requestBodyOwnQuiz = objectMapper.writeValueAsString(attemptFormOwnQuiz);
		String requestURIOwnQuiz = requestURI + quiz1User1Id;
		mockMvc.perform(post(requestURIOwnQuiz).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyOwnQuiz)).andExpect(status().isConflict())
				.andExpect(content().string("It's impossible to send attempt for your own quiz"));
	}

	@Test
	@Rollback
	public void testSendAttemptAnswersSizeMissmatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<AttemptAnswer> attemptAnswers = new ArrayList<AttemptAnswer>();
		AttemptAnswer attemptAnswer = new AttemptAnswer(Long.valueOf(1), Long.valueOf(2));
		attemptAnswers.add(attemptAnswer);

		AttemptForm attemptFormAnswersSizeMissmatch = new AttemptForm(attemptAnswers, 5);

		String requestBodyAnswersSizeMissmatch = objectMapper.writeValueAsString(attemptFormAnswersSizeMissmatch);
		String requestURIAnswersSizeMissmatch = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIAnswersSizeMissmatch).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyAnswersSizeMissmatch))
				.andExpect(status().isBadRequest()).andExpect(content().string(
						"The amount of answers in the request body doesn't match the amount of questions in the quiz"));
	}

	@Test
	@Rollback
	public void testSendAttemptQuestionNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		attemptAnswers.get(0).setQuestionId(Long.valueOf(200));

		AttemptForm attemptFormQuestionNotFound = new AttemptForm(attemptAnswers, 5);

		String requestBodyQuestionNotFound = objectMapper.writeValueAsString(attemptFormQuestionNotFound);
		String requestURIQuestionNotFound = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIQuestionNotFound).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyQuestionNotFound))
				.andExpect(status().isBadRequest()).andExpect(content().string("Question not found"));
	}

	@Test
	@Rollback
	public void testSendAttemptAnswerNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		attemptAnswers.get(0).setAnswerId(Long.valueOf(200));

		AttemptForm attemptFormAnswerNotFound = new AttemptForm(attemptAnswers, 5);

		String requestBodyAnswerNotFound = objectMapper.writeValueAsString(attemptFormAnswerNotFound);
		String requestURIAnswerNotFound = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIAnswerNotFound).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyAnswerNotFound))
				.andExpect(status().isBadRequest()).andExpect(content().string("Answer not found"));
	}

	@Test
	@Rollback
	public void testSendAttemptAnswersQuestionDontMatchCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		AttemptAnswer attemptAnswerQuestion2 = attemptAnswers.get(1);

		attemptAnswers.get(0).setAnswerId(attemptAnswerQuestion2.getAnswerId());

		AttemptForm attemptFormAnswersQuestionDontMatch = new AttemptForm(attemptAnswers, 5);

		String requestBodyAnswersQuestionDontMatch = objectMapper
				.writeValueAsString(attemptFormAnswersQuestionDontMatch);
		String requestURIAnswersQuestionDontMatch = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIAnswersQuestionDontMatch).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyAnswersQuestionDontMatch))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("One or more answers don't match corresponding question"));
	}

	@Test
	@Rollback
	public void testSendAttemptQuestionDontMatchQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Quiz quiz2User2 = user2.getQuizzes().get(1);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz2User2 = quiz2User2.getQuestions();
		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		Question question1OfQuiz2User2 = questionsOfQuiz2User2.get(0);
		Long question1OfQuiz2User2Id = question1OfQuiz2User2.getQuestionId();

		attemptAnswers.get(0).setQuestionId(question1OfQuiz2User2Id);
		attemptAnswers.get(0).setAnswerId(question1OfQuiz2User2.getAnswers().get(0).getAnswerId());

		AttemptForm attemptFormQuestionDontMatchQuiz = new AttemptForm(attemptAnswers, 5);

		String requestBodyQuestionDontMatchQuiz = objectMapper.writeValueAsString(attemptFormQuestionDontMatchQuiz);
		String requestURIQuestionDontMatchQuiz = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIQuestionDontMatchQuiz).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBodyQuestionDontMatchQuiz))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Some of questions are not in the corresponding quiz"));
	}

	@Test
	@Rollback
	public void testSendAttemptAllCorrectGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		AttemptForm attemptForm = new AttemptForm(attemptAnswers, 5);

		String requestBody = objectMapper.writeValueAsString(attemptForm);
		String requestURIGood = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());

		Difficulty quiz1User2Difficulty = quiz1User2.getDifficulty();
		Integer rate = quiz1User2Difficulty.getRate();
		Integer questionsSize = questionsOfQuiz1User2.size();
		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		UserPublic user1Public = userRepository.findRatingByUserId(user1Id);
		Integer user1Rating = user1Public.getRating();
		assertThat(user1Rating).isEqualTo(rate * questionsSize);
	}

	@Test
	@Rollback
	public void testSendAttemptNotAllCorrectGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/sendattempt/";

		User user2 = userRepository.findByUsername("user2").get();
		Quiz quiz1User2 = user2.getQuizzes().get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		List<Question> questionsOfQuiz1User2 = quiz1User2.getQuestions();

		List<AttemptAnswer> attemptAnswers = this.createCorrectAttemptAnswersList(questionsOfQuiz1User2);

		Question question1OfQuestionsOfQuiz1User2 = questionsOfQuiz1User2.get(0);
		Long incorrectAnswerOfQuestion1Id = null;
		for (Answer answer : question1OfQuestionsOfQuiz1User2.getAnswers()) {
			if (!answer.isCorrect()) {
				incorrectAnswerOfQuestion1Id = answer.getAnswerId();
				break;
			}
		}

		attemptAnswers.get(0).setAnswerId(incorrectAnswerOfQuestion1Id);

		AttemptForm attemptForm = new AttemptForm(attemptAnswers, 5);

		String requestBody = objectMapper.writeValueAsString(attemptForm);
		String requestURIGood = requestURI + quiz1User2Id;
		mockMvc.perform(post(requestURIGood).header("Authorization", jwtToken).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk());

		Difficulty quiz1User2Difficulty = quiz1User2.getDifficulty();
		Integer rate = quiz1User2Difficulty.getRate();
		Integer questionsSize = questionsOfQuiz1User2.size();
		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		UserPublic user1Public = userRepository.findRatingByUserId(user1Id);
		Integer user1Rating = user1Public.getRating();
		assertThat(user1Rating).isEqualTo(rate * (questionsSize - 1));
	}

	@Test
	@Rollback
	public void testDeleteQuizByIdNotFoundCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequiz/";

		String requestURINotFound = requestURI + Long.valueOf(200);

		mockMvc.perform(delete(requestURINotFound).header("Authorization", jwtToken)).andExpect(status().isBadRequest())
				.andExpect(content().string("The quiz was not found for provided ID"));
	}

	@Test
	@Rollback
	public void testDeleteQuizByIdOtherUserQuizCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequiz/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();
		List<Quiz> user2Quizzes = quizRepository.findQuizzesByUserId(user2Id);
		Quiz quiz1User2 = user2Quizzes.get(0);
		Long quiz1User2Id = quiz1User2.getQuizId();

		String requestURIOtherUserQuiz = requestURI + quiz1User2Id;

		mockMvc.perform(delete(requestURIOtherUserQuiz).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("You can't delete someone else's quiz"));
	}

	@Test
	@Rollback
	public void testDeleteQuizByIdGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/deletequiz/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();
		List<Quiz> user1Quizzes = quizRepository.findQuizzesByUserId(user1Id);
		Quiz quiz1User1 = user1Quizzes.get(0);
		Long quiz1User1Id = quiz1User1.getQuizId();

		String requestURIGoodCase = requestURI + quiz1User1Id;

		mockMvc.perform(delete(requestURIGoodCase).header("Authorization", jwtToken)).andExpect(status().isOk());
		Optional<Quiz> optionalQuizDeleted = quizRepository.findById(quiz1User1Id);
		assertThat(optionalQuizDeleted).isNotPresent();
	}

	@Test
	@Rollback
	public void testGetAvatarByUserIdOtherUserCase() throws Exception {
		String requestURI = END_POINT_PATH + "/getavatar/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();

		String requestURIOtherUser = requestURI + user2Id;

		mockMvc.perform(get(requestURIOtherUser).header("Authorization", jwtToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Rollback
	public void testGetAvatarByUserIdGoodCases() throws Exception {
		String requestURI = END_POINT_PATH + "/getavatar/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String requestURIGoodCase = requestURI + user1Id;

		MvcResult result = mockMvc.perform(get(requestURIGoodCase).header("Authorization", jwtToken))
				.andExpect(status().isOk()).andReturn();

		String avatarURL = result.getResponse().getContentAsString();
		assertThat(avatarURL).isEqualTo("");

		String url = "https://some-url.com";
		user1.setAvatarUrl(url);
		userRepository.save(user1);

		result = mockMvc.perform(get(requestURIGoodCase).header("Authorization", jwtToken)).andExpect(status().isOk())
				.andReturn();

		avatarURL = result.getResponse().getContentAsString();
		assertThat(avatarURL).isEqualTo(url);
	}

	@Test
	@Rollback
	public void testUpdateAvatarByUserIdOtherUserCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updateavatar/";

		User user2 = userRepository.findByUsername("user2").get();
		Long user2Id = user2.getId();

		String requestURIOtherUser = requestURI + user2Id;
		String requestBody = "some-new-url.com";

		mockMvc.perform(post(requestURIOtherUser).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isUnauthorized())
				.andExpect(content().string("You can't change someone else's avatarUrl"));
	}

	@Test
	@Rollback
	public void testUpdateAvatarByUserIdGoodCase() throws Exception {
		String requestURI = END_POINT_PATH + "/updateavatar/";

		User user1 = userRepository.findByUsername("user1").get();
		Long user1Id = user1.getId();

		String requestURIGoodCase = requestURI + user1Id;

		String requestBody = "some-new-url.com";

		mockMvc.perform(post(requestURIGoodCase).header("Authorization", jwtToken)
				.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isOk());

		user1 = userRepository.findByUsername("user1").get();
		assertThat(user1.getAvatarUrl()).isEqualTo(requestBody);
	}

	private List<AttemptAnswer> createCorrectAttemptAnswersList(List<Question> questions) {
		List<AttemptAnswer> attemptAnswers = new ArrayList<AttemptAnswer>();
		AttemptAnswer attemptAnswer;
		Long questionId;
		Long answerId = null;
		List<Answer> questionAnswers;

		for (Question question : questions) {
			questionId = question.getQuestionId();
			questionAnswers = question.getAnswers();
			for (Answer answer : questionAnswers) {
				answerId = answer.getAnswerId();
				if (answer.isCorrect())
					break;
			}
			attemptAnswer = new AttemptAnswer(answerId, questionId);
			attemptAnswers.add(attemptAnswer);
		}

		return attemptAnswers;
	}

	private QuizUpdate createQuizUpdateInstance(Quiz quiz, Long userId) {
		String title = quiz.getTitle();
		String description = quiz.getDescription();
		Integer minutes = quiz.getMinutes();
		String status = quiz.getStatus();
		Long quizId = quiz.getQuizId();
		Category otherCategory = categoryRepository.findByName("Other").get();
		Long otherCategoryId = otherCategory.getCategoryId();
		Difficulty hardDifficulty = difficultyRepository.findByName("Hard").get();
		Long hardDifficultyId = hardDifficulty.getDifficultyId();
		Double rating = attemptRepository.findQuizRating(quizId);

		QuizUpdate quizUpdate = new QuizUpdate(quizId, title, description, hardDifficultyId, minutes, rating, status,
				userId, otherCategoryId);

		return quizUpdate;
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
