package com.my.quiztaker.repositorytests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class CascadeDeletionTest {
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
	public void resetAllAndAddData() {
		this.resetAll();
		this.addData();
	}

	@Test
	@Rollback
	public void testDeleteCategoryCascade() {
		Category otherCategory = categoryRepository.findByName("Other").get();
		categoryRepository.delete(otherCategory);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<User> users = (List<User>) userRepository.findAll();
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		List<Question> questions = (List<Question>) questionRepository.findAll();

		assertThat(answers).hasSize(16);// 8 answers for each quiz; initial: 32
		assertThat(quizzes).hasSize(2); // initial: 4 quizzes, quizzes in other category: 2
		assertThat(attempts).hasSize(2); // 1 attempt for each quiz; initial: 4
		assertThat(users).hasSize(3); // initial: 3
		assertThat(difficulties).hasSize(3); // initial 3
		assertThat(questions).hasSize(4); // 2 questions for each quiz; initial: 8

	}

	@Test
	@Rollback
	public void testDeleteDifficultyCascade() {
		// Intiially one quiz in this difficulty
		Difficulty hardDifficulty = difficultyRepository.findByName("Hard").get();
		difficultyRepository.delete(hardDifficulty);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<User> users = (List<User>) userRepository.findAll();
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		List<Question> questions = (List<Question>) questionRepository.findAll();

		assertThat(answers).hasSize(24);// 8 answers for each quiz; initial: 32
		assertThat(quizzes).hasSize(3); // initial: 4 quizzes, quizzes in other category: 2
		assertThat(attempts).hasSize(3); // 1 attempt for each quiz; initial: 4
		assertThat(users).hasSize(3); // initial: 3
		assertThat(categories).hasSize(2); // initial 2
		assertThat(questions).hasSize(6); // 2 questions for each quiz; initial: 8
	}

	@Test
	@Rollback
	public void testDeleteQuizCascade() {
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		Quiz quiz = quizzes.get(0);
		quizRepository.delete(quiz);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<User> users = (List<User>) userRepository.findAll();
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		List<Question> questions = (List<Question>) questionRepository.findAll();

		assertThat(answers).hasSize(24);// 8 answers for each quiz; initial: 32
		assertThat(difficulties).hasSize(3); // initial: 3
		assertThat(attempts).hasSize(3); // 1 attempt for each quiz; initial: 4
		assertThat(users).hasSize(3); // initial: 3
		assertThat(categories).hasSize(2); // initial 2
		assertThat(questions).hasSize(6); // 2 questions for each quiz; initial: 8
	}

	// Testing deleting user that took attempts but didn't create quizzes
	@Test
	@Rollback
	public void testDeleteUserAttemptsCascade() {
		// 4 attempts for this user
		User user0 = userRepository.findByUsername("user0").get();
		userRepository.delete(user0);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		List<Question> questions = (List<Question>) questionRepository.findAll();

		assertThat(answers).hasSize(32);// 8 answers for each quiz; initial: 32
		assertThat(difficulties).hasSize(3); // initial: 3
		assertThat(attempts).hasSize(0); // 1 attempt for each quiz; initial: 4
		assertThat(quizzes).hasSize(4); // initial: 4
		assertThat(categories).hasSize(2); // initial 2
		assertThat(questions).hasSize(8); // 2 questions for each quiz; initial: 8
	}

	// Testing deleting user created quizzes but didn't took attempts
	@Test
	@Rollback
	public void testDeleteUserQuizzesCascade() {
		// 2 quizzes for this user
		User user1 = userRepository.findByUsername("user1").get();
		userRepository.delete(user1);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		List<Question> questions = (List<Question>) questionRepository.findAll();

		assertThat(answers).hasSize(16);// 8 answers for each quiz; initial: 32
		assertThat(difficulties).hasSize(3); // initial: 3
		assertThat(attempts).hasSize(2); // 1 attempt for each quiz; initial: 4
		assertThat(quizzes).hasSize(2); // initial: 4
		assertThat(categories).hasSize(2); // initial 2
		assertThat(questions).hasSize(4); // 2 questions for each quiz; initial: 8
	}

	@Test
	@Rollback
	public void testDeleteQuestionCascade() {
		List<Question> questions = (List<Question>) questionRepository.findAll();
		Question question = questions.get(0);
		questionRepository.delete(question);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		List<Attempt> attempts = (List<Attempt>) attemptRepository.findAll();
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAll();
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		List<User> users = (List<User>) userRepository.findAll();

		assertThat(answers).hasSize(28);// 4 answers for each question; initial: 32
		assertThat(difficulties).hasSize(3); // initial: 3
		assertThat(attempts).hasSize(4); // 1 attempt for each quiz; initial: 4
		assertThat(quizzes).hasSize(4); // initial: 4
		assertThat(categories).hasSize(2); // initial 2
		assertThat(users).hasSize(3); // initial: 3
	}

	private void resetAll() {
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

		// two quizzes in category other and easy difficulty: one for
		// user1, one for user2
		this.createQuizzesCategoryOtherDifficultyEasy();
		// two quizzes in category IT: one for user1 with difficulty hard and one for
		// user2 with difficulty medium
		this.createQuizzesCategoryITDifficultiesHardMedium();

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

	// Creating two quizzes with category Other and difficulty easy: for user1 and
	// user2
	private void createQuizzesCategoryOtherDifficultyEasy() {
		Category otherCategory = categoryRepository.findByName("Other").get();
		Difficulty easyDifficulty = difficultyRepository.findByName("Easy").get();

		User user1 = userRepository.findByUsername("user1").get();
		User user2 = userRepository.findByUsername("user2").get();

		Quiz newQuizUser1 = new Quiz(user1, otherCategory, easyDifficulty);
		Quiz newQuizUser2 = new Quiz(user2, otherCategory, easyDifficulty);
		quizRepository.save(newQuizUser1);
		quizRepository.save(newQuizUser2);
	}

	// Creating two quizzes with category IT:
	// 1. with category IT and difficulty hard for user1;
	// 2. with catgeory IT and difficulty medium for user2;
	private void createQuizzesCategoryITDifficultiesHardMedium() {
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
