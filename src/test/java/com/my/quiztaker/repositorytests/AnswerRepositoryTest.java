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

import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
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
public class AnswerRepositoryTest {
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
	private UserRepository userRepository;

	// deleting all hardcoded records in db and populating with default records to
	// allow creating answers
	@BeforeAll
	public void resetRepos() {
		this.deleteAll();
		this.populateWithNewData();
	}

	// test Create functionality:
	@Test
	@Rollback
	public void testCreateAnswer() {
		Question question = this.findQuestion();

		Answer defaultAnswer = this.createDefaultAnswer(question);
		assertThat(defaultAnswer.getAnswerId()).isNotNull();
		assertThat(defaultAnswer.getText()).isEqualTo("Answer");

		String answerText = "Miami";
		Answer answerWithText = new Answer(answerText, false, question);
		answerRepository.save(answerWithText);
		assertThat(answerWithText.getAnswerId()).isNotNull();
		assertThat(answerWithText.getText()).isEqualTo(answerText);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).hasSize(2);
	}

	// Test create Default answers method:
	@Test
	@Rollback
	public void testCreateDefaultAnswers() {
		Question question = this.findQuestion();

		this.createDefaultAnswers(question);

		List<Answer> answers = question.getAnswers();
		assertThat(answers).hasSize(4);
		
		answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).hasSize(4);

		int rightAnswersQuantity = 0;
		for (Answer answer : answers) {
			if (answer.isCorrect())
				rightAnswersQuantity++;
			assertThat(answer.getText()).isEqualTo("Answer");
		}
		assertThat(rightAnswersQuantity).isEqualTo(1);
	}

	// Test creating custom answers:
	@Test
	@Rollback
	public void testCreateCustomAnswers() {
		Question question = this.findQuestion();

		this.createCustomAnswers(question);

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).hasSize(4);

		int rightAnswersQuantity = 0;
		for (Answer answer : answers) {
			if (answer.isCorrect()) {
				rightAnswersQuantity++;
				assertThat(answer.getText()).isEqualTo("Correct");
			}
		}
		assertThat(rightAnswersQuantity).isEqualTo(1);
	}

	// Test find answers by question id;
	@Test
	@Rollback
	public void testFindByQuestionId() {
		Question question1 = this.findQuestion();
		
		Long question1Id = question1.getQuestionId();
		this.createDefaultAnswers(question1);

		List<Answer> answersOfQuestion1 = answerRepository.findByQuestionId(question1Id);
		assertThat(answersOfQuestion1).hasSize(4);
	}

	// Test find correct answer by question id:
	@Test
	@Rollback
	public void testFindCorrectByQuestionId() {
		Question question1 = this.findQuestion();
		
		Long question1Id = question1.getQuestionId();
		this.createDefaultAnswers(question1);

		Answer correctAnswer = answerRepository.findCorrectByQuestionId(question1Id);
		assertThat(correctAnswer).isNotNull();
		assertThat(correctAnswer.isCorrect()).isTrue();
	}

	// Test find answer texts by question Id;
	@Test
	@Rollback
	private void testFindAnswerTextsByQuestionId() {
		List<Question> questions = (List<Question>) questionRepository.findAll();
		Question question1 = questions.get(0);
		
		Long question1Id = question1.getQuestionId();
		Question question2 = questions.get(1);
		Long question2Id = question2.getQuestionId();
		this.createDefaultAnswers(question1);
		this.createCustomAnswers(question2);

		List<String> answersTexts1 = answerRepository.findAnswerTextsByQuestionId(question1Id);
		assertThat(answersTexts1).hasSize(4);
		
		assertThat(question1.getAnswers()).hasSize(4);

		for (String answerText : answersTexts1) {
			assertThat(answerText).isEqualTo("Answer");
		}

		List<String> answersTexts2 = answerRepository.findAnswerTextsByQuestionId(question2Id);
		assertThat(answersTexts2).hasSize(4);
	}

	// Test findAll and findById:
	public void testFindAllAndFindById() {
		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).isEmpty();

		Question question = this.findQuestion();

		Answer newAnswer = new Answer(question, true);
		Long newAnswerId = newAnswer.getAnswerId();
		Optional<Answer> optionalAnswer = answerRepository.findById(newAnswerId);
		assertThat(optionalAnswer.get()).isNull();

		answerRepository.save(newAnswer);

		answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).hasSize(0);

		optionalAnswer = answerRepository.findById(newAnswerId);
		assertThat(optionalAnswer).isPresent();
	}

	// Test update answer functionality
	@Test
	@Rollback
	public void testUpdateAnswer() {
		Question question = this.findQuestion();

		Answer newAnswer = this.createDefaultAnswer(question);

		Long newAnswerId = newAnswer.getAnswerId();
		newAnswer.setCorrect(false);
		newAnswer.setText("Miami");
		answerRepository.save(newAnswer);

		Optional<Answer> optionalAnswer = answerRepository.findById(newAnswerId);
		assertThat(optionalAnswer).isPresent();

		Answer answer = optionalAnswer.get();
		assertThat(answer.getText()).isEqualTo("Miami");
		assertThat(answer.isCorrect()).isFalse();
	}

	// Test delete answer functionalities:
	@Test
	@Rollback
	public void testDeleteAnswer() {
		Question question = this.findQuestion();

		Answer newAnswer = this.createDefaultAnswer(question);
		Long newAnswerId = newAnswer.getAnswerId();
		answerRepository.deleteById(newAnswerId);

		Optional<Answer> optionalAnswerNull = answerRepository.findById(newAnswerId);
		assertThat(optionalAnswerNull).isNotPresent();

		this.createDefaultAnswers(question);
		answerRepository.deleteAll();

		List<Answer> answers = (List<Answer>) answerRepository.findAll();
		assertThat(answers).hasSize(0);
	}
	
	private Question findQuestion() {
		List<Question> questions = (List<Question>) questionRepository.findAll();
		Question question = questions.get(0);
		
		return question;
	}
	
	private Answer createDefaultAnswer(Question question) {
		Answer defaultAnswer = new Answer(question, true);
		answerRepository.save(defaultAnswer);
		
		return defaultAnswer;
	}

	private void deleteAll() {
		answerRepository.deleteAll();
		questionRepository.deleteAll();
		quizRepository.deleteAll();
		categoryRepository.deleteAll();
		difficultyRepository.deleteAll();
		userRepository.deleteAll();
	}

	private void populateWithNewData() {
		User user = this.createUser();
		Difficulty difficulty = this.createDifficulty();
		Category category = this.createCategory();
		Quiz quiz = this.createQuiz(user, difficulty, category);

		// create two questions:
		this.createQuestion(quiz);
		this.createQuestion(quiz);
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

	private Quiz createQuiz(User user, Difficulty difficulty, Category category) {
		Quiz newQuiz = new Quiz(user, category, difficulty);
		quizRepository.save(newQuiz);

		return newQuiz;
	}

	private void createQuestion(Quiz quiz) {
		Question newQuestion = new Question(quiz);
		questionRepository.save(newQuestion);
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

	private void createCustomAnswers(Question question) {
		Answer customAnswer1 = new Answer("Correct", true, question);
		Answer customAnswer2 = new Answer("Wrong1", false, question);
		Answer customAnswer3 = new Answer("Wrong2", false, question);
		Answer customAnswer4 = new Answer("Wrong3", false, question);

		answerRepository.save(customAnswer1);
		answerRepository.save(customAnswer2);
		answerRepository.save(customAnswer3);
		answerRepository.save(customAnswer4);
	}
}
