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

import com.my.quiztaker.model.Attempt;
import com.my.quiztaker.model.AttemptRepository;
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
public class AttemptRepositoryTest {
	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeAll
	public void resetRepos() {
		attemptRepository.deleteAll();
		quizRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		userRepository.deleteAll();
	}

	// CRUD functionalities test
	@Test
	@Rollback
	public void testCreateDefault() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Attempt defaultAttmept = this.createDefaultAttempt(quiz, user);
		Long defaultAttmeptId = defaultAttmept.getAttemptId();
		int defaultAttmeptRating = 5;

		Optional<Attempt> optionalAttempt = attemptRepository.findById(defaultAttmeptId);
		assertThat(optionalAttempt).isPresent();

		Attempt attempt = optionalAttempt.get();
		assertThat(attempt.getScore()).isNull();
		assertThat(attempt.getRating()).isEqualTo(defaultAttmeptRating);
	}

	@Test
	@Rollback
	public void testCreateCustom() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		int customAttemptScore = 11;
		int customAttmeptRating = 4;

		Attempt customAttempt = this.createCustomAttempt(customAttemptScore, quiz, user, customAttmeptRating);
		Long customAttemptId = customAttempt.getAttemptId();

		Optional<Attempt> optionalAttempt = attemptRepository.findById(customAttemptId);
		assertThat(optionalAttempt).isPresent();

		Attempt attempt = optionalAttempt.get();
		assertThat(attempt.getScore()).isEqualTo(customAttemptScore);
		assertThat(attempt.getRating()).isEqualTo(customAttmeptRating);
	}

	// Test Read functionalities:
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		assertThat(attempts).isEmpty();

		Optional<Attempt> optionalAttempt = attemptRepository.findById(Long.valueOf(2));
		assertThat(optionalAttempt).isNotPresent();

		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Attempt defaultAttempt = this.createDefaultAttempt(quiz, user);
		Long defaultAttemptId = defaultAttempt.getAttemptId();

		optionalAttempt = attemptRepository.findById(defaultAttemptId);
		assertThat(optionalAttempt).isPresent();

		this.createDefaultAttempt(quiz, user);
		attempts = (List<Attempt>) attemptRepository.findAll();
		assertThat(attempts).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindQuizRating() {
		User user = this.createUser();
		Quiz quiz1 = this.createQuiz(user);
		Long quiz1Id = quiz1.getQuizId();
		
		Double quiz1Rating = attemptRepository.findQuizRating(quiz1Id);
		assertThat(quiz1Rating).isNull();

		this.createDefaultAttempt(quiz1, user);
		quiz1Rating = attemptRepository.findQuizRating(quiz1Id);
		assertThat(quiz1Rating).isEqualTo(5);
		
		this.createCustomAttempt(4, quiz1, user, 4);
		quiz1Rating = attemptRepository.findQuizRating(quiz1Id);
		assertThat(quiz1Rating).isBetween(4.0, 5.0);
	}
	
	@Test
	@Rollback
	public void testFindAttemptsByUserId() {
		User user1 = this.createUser();
		User user2 = this.createCustomUser("user2", "user2@mail.com");
		Quiz quiz1 = this.createQuiz(user1);
		Quiz quiz2 = this.createQuiz(user2);
		Long user1Id = user1.getId();
		
		Integer attemptsForUser1 = attemptRepository.findAttemptsByUserId(user1Id);
		assertThat(attemptsForUser1).isEqualTo(0);

		this.createDefaultAttempt(quiz1, user1);
		this.createDefaultAttempt(quiz2, user1);
		this.createDefaultAttempt(quiz1, user2);
		attemptsForUser1 = attemptRepository.findAttemptsByUserId(user1Id);
		assertThat(attemptsForUser1).isEqualTo(2);
	}
	
	@Test
	@Rollback
	public void testFindAttemptsForTheQuizByUserId() {
		User user1 = this.createUser();
		User user2 = this.createCustomUser("user2", "user2@mail.com");
		Quiz quiz1 = this.createQuiz(user1);
		Quiz quiz2 = this.createQuiz(user2);
		
		Long quiz2Id = quiz2.getQuizId();
		Long user1Id = user1.getId();
		
		Integer attemptsForUser1Quiz2 = attemptRepository.findAttemptsForTheQuizByUserId(user1Id, quiz2Id);
		assertThat(attemptsForUser1Quiz2).isEqualTo(0);

		this.createDefaultAttempt(quiz2, user1);
		this.createDefaultAttempt(quiz1, user1);
		this.createDefaultAttempt(quiz1, user2);
		attemptsForUser1Quiz2 = attemptRepository.findAttemptsForTheQuizByUserId(user1Id, quiz2Id);
		assertThat(attemptsForUser1Quiz2).isEqualTo(1);
	}

	// Test update
	@Test
	@Rollback
	public void testUpdateAttempt() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Attempt defaultAttempt = this.createDefaultAttempt(quiz, user);
		Long defaultAttemptId = defaultAttempt.getAttemptId();

		int customScore = 11;
		int customRating = 1;
		
		defaultAttempt.setRating(customRating);
		defaultAttempt.setScore(customScore);

		Optional<Attempt> optionalAttempt = attemptRepository.findById(defaultAttemptId);
		assertThat(optionalAttempt).isPresent();

		Attempt updatedAttempt = optionalAttempt.get();
		assertThat(updatedAttempt.getScore()).isEqualTo(customScore);
		assertThat(updatedAttempt.getRating()).isEqualTo(customRating);
	}

	// Test delete funcitonality:
	@Test
	@Rollback
	public void testDeleteAttempt() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Attempt defaultAttempt = this.createDefaultAttempt(quiz, user);
		Long defaultAttemptId = defaultAttempt.getAttemptId();

		attemptRepository.deleteById(defaultAttemptId);

		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		assertThat(attempts).isEmpty();

		this.createDefaultAttempt(quiz, user);
		this.createDefaultAttempt(quiz, user);

		attemptRepository.deleteAll();

		attempts = (List<Attempt>) attemptRepository.findAll();
		assertThat(attempts).isEmpty();
	}

	private Attempt createDefaultAttempt(Quiz quiz, User user) {
		Attempt defaultAttempt = new Attempt(quiz, user);
		attemptRepository.save(defaultAttempt);

		return defaultAttempt;
	}

	private Attempt createCustomAttempt(int score, Quiz quiz, User user, int rating) {
		Attempt customAttempt = new Attempt(score, quiz, user, rating);
		attemptRepository.save(customAttempt);

		return customAttempt;
	}

	private User createUser() {
		User newUser = new User("user1", "passworHash", "USER", "new@mail.com", null, true);
		userRepository.save(newUser);

		return newUser;
	}
	
	private User createCustomUser(String username, String email) {
		User customUser = new User(username, "passworHash", "USER", email, null, true);
		userRepository.save(customUser);

		return customUser;
	}

	private Difficulty createDifficulty() {
		Difficulty hardDifficulty = new Difficulty("Hard", 1.0);
		difficultyRepository.save(hardDifficulty);

		return hardDifficulty;
	}

	private Category createCategory() {
		Category newCategory = new Category("IT");
		categoryRepository.save(newCategory);

		return newCategory;
	}

	private Quiz createQuiz(User user) {
		Category category = this.createCategory();
		Difficulty difficulty = this.createDifficulty();

		Quiz newQuiz = new Quiz(user, category, difficulty);
		quizRepository.save(newQuiz);

		return newQuiz;
	}
}
