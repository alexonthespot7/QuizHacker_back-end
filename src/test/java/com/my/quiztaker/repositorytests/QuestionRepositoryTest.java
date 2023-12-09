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
public class QuestionRepositoryTest {
	@Autowired
	private QuestionRepository questionRepository;

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
		questionRepository.deleteAll();
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

		Question defaultQuestion = this.createDefaultQuestion(quiz);
		Long defaultQuestionId = defaultQuestion.getQuestionId();
		String defaultQuestionText = "Question Text";

		Optional<Question> optionalQuestion = questionRepository.findById(defaultQuestionId);
		assertThat(optionalQuestion).isPresent();

		Question question = optionalQuestion.get();
		assertThat(question.getText()).isEqualTo(defaultQuestionText);
	}

	@Test
	@Rollback
	public void testCreateCustom() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		String customQuestionText = "How old is Leo Messi";

		Question customQuestion = this.createCustomQuestion(customQuestionText, quiz);
		Long customQuestionId = customQuestion.getQuestionId();

		Optional<Question> optionalQuestion = questionRepository.findById(customQuestionId);
		assertThat(optionalQuestion).isPresent();

		Question question = optionalQuestion.get();
		assertThat(question.getText()).isEqualTo(customQuestionText);
	}

	// Test Read functionalities:
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Question> questions = (List<Question>) questionRepository.findAll();
		assertThat(questions).isEmpty();

		Optional<Question> optionalQuestion = questionRepository.findById(Long.valueOf(2));
		assertThat(optionalQuestion).isNotPresent();

		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Question defaultQuestion = this.createDefaultQuestion(quiz);
		Long defaultQuestionId = defaultQuestion.getQuestionId();

		optionalQuestion = questionRepository.findById(defaultQuestionId);
		assertThat(optionalQuestion).isPresent();

		this.createDefaultQuestion(quiz);
		questions = (List<Question>) questionRepository.findAll();
		assertThat(questions).hasSize(2);
	}

	@Test
	@Rollback
	public void testFindQuestionsByQuizId() {
		User user = this.createUser();
		Quiz quiz1 = this.createQuiz(user);

		Long quiz1Id = quiz1.getQuizId();

		List<Question> questionsOfQuiz1 = (List<Question>) questionRepository.findQuestionsByQuizId(quiz1Id);
		assertThat(questionsOfQuiz1).isEmpty();

		this.createDefaultQuestion(quiz1);
		this.createDefaultQuestion(quiz1);
		Quiz quiz2 = this.createQuiz(user);
		Long quiz2Id = quiz2.getQuizId();
		this.createDefaultQuestion(quiz2);

		questionsOfQuiz1 = (List<Question>) questionRepository.findQuestionsByQuizId(quiz1Id);
		assertThat(questionsOfQuiz1).hasSize(2);

		List<Question> questionsOfQuiz2 = (List<Question>) questionRepository.findQuestionsByQuizId(quiz2Id);
		assertThat(questionsOfQuiz2).hasSize(1);
	}

	// Test update
	@Test
	@Rollback
	public void testUpdateQuestion() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Question defaultQuestion = this.createDefaultQuestion(quiz);
		Long defaultQuestionId = defaultQuestion.getQuestionId();

		String customText = "Who is the richest person in the world?";
		defaultQuestion.setText(customText);

		Optional<Question> optionalQuestion = questionRepository.findById(defaultQuestionId);
		assertThat(optionalQuestion).isPresent();

		Question updatedQuestion = optionalQuestion.get();
		assertThat(updatedQuestion.getText()).isEqualTo(customText);
	}

	// Test delete funcitonality:
	@Test
	@Rollback
	public void testDeleteQuestion() {
		User user = this.createUser();
		Quiz quiz = this.createQuiz(user);

		Question defaultQuestion = this.createDefaultQuestion(quiz);
		Long defaultQuestionId = defaultQuestion.getQuestionId();

		questionRepository.deleteById(defaultQuestionId);

		List<Question> questions = (List<Question>) questionRepository.findAll();
		assertThat(questions).isEmpty();

		this.createDefaultQuestion(quiz);
		this.createDefaultQuestion(quiz);

		questionRepository.deleteAll();

		questions = (List<Question>) questionRepository.findAll();
		assertThat(questions).isEmpty();
	}

	private User createUser() {
		User newUser = new User("user1", "passworHash", "USER", "new@mail.com", null, true);
		userRepository.save(newUser);

		return newUser;
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

	private Question createDefaultQuestion(Quiz quiz) {
		Question defaultQuestion = new Question(quiz);
		questionRepository.save(defaultQuestion);

		return defaultQuestion;
	}

	private Question createCustomQuestion(String text, Quiz quiz) {
		Question customQuestion = new Question(text, quiz);
		questionRepository.save(customQuestion);

		return customQuestion;
	}
}
