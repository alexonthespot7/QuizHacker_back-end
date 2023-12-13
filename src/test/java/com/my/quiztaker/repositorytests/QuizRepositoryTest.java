package com.my.quiztaker.repositorytests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class QuizRepositoryTest {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private QuizRepository quizRepository;

	@BeforeAll
	public void resetRepos() {
		userRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		quizRepository.deleteAll();
	}

	// Test CRUD for quiz repository:

	// Testing create for quiz:
	@Test
	@Rollback
	public void testCreateDefaultQuiz() {
		User user = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		this.createDefaultQuiz(user, difficulty, category);

		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).hasSize(1);

		Quiz defaultQuiz = quizzes.get(0);
		assertThat(defaultQuiz.getMinutes()).isEqualTo(5);
		assertThat(defaultQuiz.getCategory().getName()).isEqualTo("IT");
		assertThat(defaultQuiz.getDifficulty().getName()).isEqualTo("Hard");
		assertThat(defaultQuiz.getTitle()).isEqualTo("Quiz Title");
		assertThat(defaultQuiz.getDescription()).isEqualTo("Quiz Description");
		assertThat(defaultQuiz.getStatus()).isEqualTo("Created");
	}

	@Test
	@Rollback
	public void testCreateQuizWithAllFields() {
		User user = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		String customTitle = "My Quiz";
		String customDescription = "My custom description";
		String customStatus = "Published";

		this.createQuizWithAllFields(customTitle, customDescription, category, difficulty, user, 4, customStatus);

		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).hasSize(1);

		Quiz quizWithAllFields = quizzes.get(0);
		assertThat(quizWithAllFields.getMinutes()).isEqualTo(4);
		assertThat(quizWithAllFields.getTitle()).isEqualTo(customTitle);
		assertThat(quizWithAllFields.getDescription()).isEqualTo(customDescription);
		assertThat(quizWithAllFields.getStatus()).isEqualTo(customStatus);
	}

	@Test
	@Rollback
	public void testCreateQuizWithNoDescription() {
		User user = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		String customTitle = "My Quiz";
		String customStatus = "Published";

		this.createQuizNoDescriptions(customTitle, category, difficulty, user, 3, customStatus);

		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).hasSize(1);

		Quiz quizWithNoDescription = quizzes.get(0);
		assertThat(quizWithNoDescription.getDescription()).isNull();
	}

	// Test Read Functionalities:
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).isEmpty();

		Optional<Quiz> optionalQuiz = quizRepository.findById(Long.valueOf(2));
		assertThat(optionalQuiz).isNotPresent();

		User user = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();

		this.createDefaultQuiz(user, difficulty, category);
		Quiz newQuiz1 = this.createDefaultQuiz(user, difficulty, category);
		Long newQuiz1Id = newQuiz1.getQuizId();

		quizzes = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzes).hasSize(2);

		optionalQuiz = quizRepository.findById(newQuiz1Id);
		assertThat(optionalQuiz).isPresent();
	}

	@Test
	@Rollback
	public void testFindQuizzesFromOtherUsers() {
		User user1 = this.createDefaultUser();
		Long user1Id = user1.getId();

		List<Quiz> quizzesOfOtherUsersForUser1 = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		assertThat(quizzesOfOtherUsersForUser1).isEmpty();
		
		User user2 = this.createCustomUser("user2", "user2@mail.com");
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		this.createDefaultQuiz(user2, difficulty, category);
		this.createDefaultQuiz(user2, difficulty, category);
		
		this.createDefaultQuiz(user1, difficulty, category);
		
		quizzesOfOtherUsersForUser1 = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		assertThat(quizzesOfOtherUsersForUser1).hasSize(0);
		
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		for (Quiz quiz : quizzes) {
			quiz.setStatus("Published");
			quizRepository.save(quiz);
		}
		
		quizzesOfOtherUsersForUser1 = quizRepository.findPublishedQuizzesFromOtherUsers(user1Id);
		assertThat(quizzesOfOtherUsersForUser1).hasSize(2);
	}
	
	@Test
	@Rollback
	public void testFindAllPublished() {
		List<Quiz> publishedQuizzes = quizRepository.findAllPublished();
		assertThat(publishedQuizzes).isEmpty();
		
		User user1 = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		Quiz quiz1 = this.createDefaultQuiz(user1, difficulty, category);
		quiz1.setStatus("Published");
		quizRepository.save(quiz1);
		this.createDefaultQuiz(user1, difficulty, category);
		
		publishedQuizzes = quizRepository.findAllPublished();
		assertThat(publishedQuizzes).hasSize(1);
	}
	
	// Testing update:
	@Test
	@Rollback
	public void testUpdateQuiz() {
		User user1 = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		Quiz quiz1 = this.createDefaultQuiz(user1, difficulty, category);
		Long quiz1Id = quiz1.getQuizId();

		String customTitle = "My title";
		String customDescription = "My description";
		String publishedStatus = "Published";
		
		quiz1.setDescription(customDescription);
		quiz1.setTitle(customTitle);
		quiz1.setMinutes(6);
		quiz1.setStatus(publishedStatus);
		quizRepository.save(quiz1);

		Optional<Quiz> optionalQuiz = quizRepository.findById(quiz1Id);
		assertThat(optionalQuiz).isPresent();

		Quiz quiz = optionalQuiz.get();
		assertThat(quiz.getDescription()).isEqualTo(customDescription);
		assertThat(quiz.getTitle()).isEqualTo(customTitle);
		assertThat(quiz.getStatus()).isEqualTo(publishedStatus);
		assertThat(quiz.getMinutes()).isEqualTo(6);
	}
	
	//Test Delete
	@Test
	@Rollback
	public void testDeleteQuiz() {
		User user1 = this.createDefaultUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		Quiz quiz1 = this.createDefaultQuiz(user1, difficulty, category);
		Long quiz1Id = quiz1.getQuizId();
		
		quizRepository.deleteById(quiz1Id);
		
		Optional<Quiz> deletedQuiz = quizRepository.findById(quiz1Id);
		assertThat(deletedQuiz).isNotPresent();
		
		this.createDefaultQuiz(user1, difficulty, category);
		this.createDefaultQuiz(user1, difficulty, category);
		
		quizRepository.deleteAll();
		
		List<Quiz> quizzesEmpty = (List<Quiz>) quizRepository.findAll();
		assertThat(quizzesEmpty).hasSize(0);
	}

	private User createDefaultUser() {
		User newUser = new User("user1", "passworHash", "USER", "new@mail.com", null, true);
		userRepository.save(newUser);

		return newUser;
	}

	private User createCustomUser(String username, String email) {
		User newUser = new User(username, "passworHash", "USER", email, null, true);
		userRepository.save(newUser);

		return newUser;
	}

	private Difficulty createDifficulty() {
		Difficulty hardDifficulty = new Difficulty("Hard", 3);
		difficultyRepository.save(hardDifficulty);

		return hardDifficulty;
	}

	private Category createCategory() {
		Category newCategory = new Category("IT");
		categoryRepository.save(newCategory);

		return newCategory;
	}

	private Quiz createDefaultQuiz(User user, Difficulty difficulty, Category category) {
		Quiz defaultQuiz = new Quiz(user, category, difficulty);
		quizRepository.save(defaultQuiz);

		return defaultQuiz;
	}

	private Quiz createQuizWithAllFields(String title, String description, Category category, Difficulty difficulty,
			User user, int minutes, String status) {
		Quiz quizWithAllFields = new Quiz(title, description, category, difficulty, user, minutes, status);
		quizRepository.save(quizWithAllFields);

		return quizWithAllFields;
	}

	private Quiz createQuizNoDescriptions(String title, Category category, Difficulty difficulty, User user,
			int minutes, String status) {
		Quiz quizWithNoDescription = new Quiz(title, category, difficulty, user, minutes, status);
		quizRepository.save(quizWithNoDescription);

		return quizWithNoDescription;
	}
}
