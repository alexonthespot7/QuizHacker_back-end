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

import com.my.quiztaker.forms.UserPublic;
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
public class UserRepositoryTest {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@BeforeAll
	public void resetUserRepo() {
		userRepository.deleteAll();
	}

	// CRUD tests for the user repository

	// Create functionality
	@Test
	@Rollback
	public void testCreateUser() {
		User newUser1 = this.createVerifiedUser("user1", "user1@mail.com");
		assertThat(newUser1.getId()).isNotNull();

		this.createVerifiedUser("user2", "user2@mail.com");
		List<User> users = (List<User>) userRepository.findAll();
		assertThat(users).hasSize(2);
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<User> users = (List<User>) userRepository.findAll();
		assertThat(users).isEmpty();

		Optional<User> optionalUser = userRepository.findById(Long.valueOf(2));
		assertThat(optionalUser).isNotPresent();

		User newUser1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long newUser1Id = newUser1.getId();
		this.createVerifiedUser("user2", "user2@mail.com");

		users = (List<User>) userRepository.findAll();
		assertThat(users).hasSize(2);

		optionalUser = userRepository.findById(newUser1Id);
		assertThat(optionalUser).isPresent();
	}

	// Custom UserRepository queries tests

	@Test
	@Rollback
	public void testFindByUsername() {
		String usernameToFindUser = "user1";
		Optional<User> optionalUser = userRepository.findByUsername(usernameToFindUser);
		assertThat(optionalUser).isNotPresent();

		this.createVerifiedUser(usernameToFindUser, "user1@mail.com");
		optionalUser = userRepository.findByUsername(usernameToFindUser);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testFindByEmail() {
		String emailToFindUser = "user1@mail.com";
		Optional<User> optionalUser = userRepository.findByEmail(emailToFindUser);
		assertThat(optionalUser).isNotPresent();

		this.createVerifiedUser("user1", emailToFindUser);
		optionalUser = userRepository.findByEmail(emailToFindUser);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testFindByVerificationCode() {
		String verificationCodeToFindUser = "mycode";
		Optional<User> optionalUser = userRepository.findByVerificationCode(verificationCodeToFindUser);
		assertThat(optionalUser).isNotPresent();

		this.createUnverifiedUser("user1", "user1@mail.com", verificationCodeToFindUser);
		optionalUser = userRepository.findByVerificationCode(verificationCodeToFindUser);
		assertThat(optionalUser).isPresent();
	}

	@Test
	@Rollback
	public void testFindLeaderboard() {
		this.resetRepos();

		List<UserPublic> usersLeaderBoard = userRepository.findLeaderBoard();
		assertThat(usersLeaderBoard).isEmpty();

		User user1 = this.createVerifiedUser("bUser1", "user1@mail.com");
		User user2 = this.createVerifiedUser("aUser2", "user2@mail.com");
		usersLeaderBoard = userRepository.findLeaderBoard();
		assertThat(usersLeaderBoard).hasSize(0);

		Quiz quizOfUser1 = this.createQuizWithHardDifficulty(user1);
		Quiz quizOfUser2 = this.createQuizWithHardDifficulty(user2);

		this.addRating(user1, quizOfUser2, 8);
		this.addRating(user2, quizOfUser1, 5);

		usersLeaderBoard = userRepository.findLeaderBoard();
		assertThat(usersLeaderBoard).hasSize(2);

		UserPublic topUser = usersLeaderBoard.get(0);
		assertThat(topUser.getUsername()).isEqualTo("bUser1");

		this.addRating(user2, quizOfUser1, 3);
		usersLeaderBoard = userRepository.findLeaderBoard();
		topUser = usersLeaderBoard.get(0);
		assertThat(topUser.getUsername()).isEqualTo("aUser2");
	}

	@Test
	@Rollback
	public void testFindRatingByUserId() {
		User user1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long user1Id = user1.getId();

		UserPublic userPublic1 = userRepository.findRatingByUserId(user1Id);
		assertThat(userPublic1).isNull();

		User user2 = this.createVerifiedUser("aUser2", "user2@mail.com");
		Long user2Id = user2.getId();

		Quiz quizOfUser1 = this.createQuizWithHardDifficulty(user1);
		Quiz quizOfUser2 = this.createQuizWithHardDifficulty(user2);

		this.addRating(user1, quizOfUser2, 8);
		this.addRating(user2, quizOfUser1, 5);

		userPublic1 = userRepository.findRatingByUserId(user1Id);
		UserPublic userPublic2 = userRepository.findRatingByUserId(user2Id);
		assertThat(userPublic1).isNotNull();
		assertThat(userPublic2).isNotNull();

		assertThat(userPublic1.getRating()).isEqualTo(8 * 3);
		assertThat(userPublic2.getRating()).isEqualTo(5 * 3);
	}
	
	@Test
	@Rollback
	public void testFindRatingByUserIdMedium() {
		User user1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long user1Id = user1.getId();

		UserPublic userPublic1 = userRepository.findRatingByUserId(user1Id);
		assertThat(userPublic1).isNull();

		User user2 = this.createVerifiedUser("aUser2", "user2@mail.com");
		Long user2Id = user2.getId();

		Quiz quizOfUser1 = this.createQuizWithMidDifficulty(user1);
		Quiz quizOfUser2 = this.createQuizWithMidDifficulty(user2);

		this.addRating(user1, quizOfUser2, 8);
		this.addRating(user2, quizOfUser1, 5);

		userPublic1 = userRepository.findRatingByUserId(user1Id);
		UserPublic userPublic2 = userRepository.findRatingByUserId(user2Id);
		assertThat(userPublic1).isNotNull();
		assertThat(userPublic2).isNotNull();

		assertThat(userPublic1.getRating()).isEqualTo(8 * 2);
		assertThat(userPublic2.getRating()).isEqualTo(5 * 2);
	}

	// Testing update functionalities:
	@Test
	@Rollback
	public void testUpdateUser() {
		User user1 = this.createVerifiedUser("user1", "user1@mail.com");

		user1.setEmail("new@mail.com");
		user1.setUsername("newUser1");
		userRepository.save(user1);

		Optional<User> optionalUpdatedUser = userRepository.findByEmail("new@mail.com");
		assertThat(optionalUpdatedUser).isPresent();

		user1 = optionalUpdatedUser.get();
		assertThat(user1.getUsername()).isEqualTo("newUser1");
	}

	// Testing delete functionalities:
	@Test
	@Rollback
	public void testDeleteUser() {
		User user1 = this.createVerifiedUser("user1", "user1@mail.com");
		Long user1Id = user1.getId();

		userRepository.deleteById(user1Id);
		List<User> users = (List<User>) userRepository.findAll();
		assertThat(users).hasSize(0);

		this.createVerifiedUser("user1", "user1@mail.com");
		this.createVerifiedUser("user2", "user2@mail.com");

		userRepository.deleteAll();
		users = (List<User>) userRepository.findAll();
		assertThat(users).hasSize(0);
	}

	private User createVerifiedUser(String username, String email) {
		User newUser = new User(username, "Some_Pwd_Hash", "USER", email, null, true);
		userRepository.save(newUser);

		return newUser;
	}

	private User createUnverifiedUser(String username, String email, String verificationCode) {
		User newUser = new User(username, "Some_Pwd_Hash", "USER", email, verificationCode, false);
		userRepository.save(newUser);

		return newUser;
	}

	private void addRating(User user, Quiz quiz, int score) {
		Attempt newAttempt = new Attempt(score, quiz, user, 4);
		attemptRepository.save(newAttempt);
	}

	private Quiz createQuizWithHardDifficulty(User user) {
		Category category = this.createCategory();

		Difficulty difficulty = this.createHardDifficulty();

		Quiz newQuiz = new Quiz(user, category, difficulty);
		quizRepository.save(newQuiz);

		return newQuiz;
	}
	
	private Quiz createQuizWithMidDifficulty(User user) {
		Category category = this.createCategory();

		Difficulty difficulty = this.createMediumDifficulty();

		Quiz newQuiz = new Quiz(user, category, difficulty);
		quizRepository.save(newQuiz);

		return newQuiz;
	}

	private void resetRepos() {
		quizRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		attemptRepository.deleteAll();
	}

	private Difficulty createHardDifficulty() {
		Difficulty hardDifficulty = new Difficulty("Hard", 3);
		difficultyRepository.save(hardDifficulty);

		return hardDifficulty;
	}
	
	private Difficulty createMediumDifficulty() {
		Difficulty mediumDifficulty = new Difficulty("Medium", 2);
		difficultyRepository.save(mediumDifficulty);

		return mediumDifficulty;
	}

	private Category createCategory() {
		Category newCategory = new Category("IT");
		categoryRepository.save(newCategory);

		return newCategory;
	}
}
