package com.my.quiztaker.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.QuizUpdate;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.AnswerRepository;
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

@Service
public class QuizService {
	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private AttemptRepository attemptRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DifficultyRepository difficultyRepository;

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private CommonService commonService;

	// Method to get published quizzes with its rating and amount of questions;
	public List<QuizRatingQuestions> getQuizzes() {
		List<Quiz> publishedQuizzes = (List<Quiz>) quizRepository.findAllPublished();

		// Using list of quizzes to create list of quizzes with its rating and amount of
		// questions
		List<QuizRatingQuestions> quizRatingQuestionsList = this
				.makeQuizRatingQuestionsListFromQuizzes(publishedQuizzes);

		return quizRatingQuestionsList;
	}

	// The method to get quiz info and its rating by quiz ID:
	public QuizRating getQuizById(Long quizId) {
		Quiz quiz = commonService.checkQuizById(quizId);

		Double rating = attemptRepository.findQuizRating(quizId);

		return new QuizRating(quiz, rating);
	}

	// Method to get published quizzes created by other users than the authenticated
	// one:
	public List<QuizRatingQuestions> getQuizzesOfOthersAuth(Long userId, Authentication auth) {
		commonService.checkAuthenticationAndRights(auth, userId);

		List<Quiz> quizzesOfOthersNoAttempts = this.getQuizzesOfOthersThatUserDidntAttempt(userId);

		List<QuizRatingQuestions> quizRatingQuestionsList = this
				.makeQuizRatingQuestionsListFromQuizzes(quizzesOfOthersNoAttempts);

		return quizRatingQuestionsList;
	}

	// Method to get quizzes created by authenticated user:
	public List<QuizRatingQuestions> getPersonalQuizzes(Long userId, Authentication auth) {
		commonService.checkAuthenticationAndRights(auth, userId);

		List<Quiz> quizzesOfUser = quizRepository.findQuizzesByUserId(userId);

		List<QuizRatingQuestions> quizRatingQuestionsList = this.makeQuizRatingQuestionsListFromQuizzes(quizzesOfUser);

		return quizRatingQuestionsList;
	}

	// Method to create new default quiz by current authentication instance:
	public ResponseEntity<?> createQuizByAuth(Authentication auth) {
		User user = commonService.checkAuthentication(auth);
		Category otherCategory = this.findCategoryByName("Other");
		Difficulty easyDifficulty = this.findDifficultyByName("Easy");

		return this.createDefaultQuiz(user, otherCategory, easyDifficulty);
	}

	// Method to update quiz for authenticated user:
	public ResponseEntity<?> updateQuizByAuth(Long quizId, QuizUpdate quizUpdated, Authentication auth) {
		Quiz quizInDB = commonService.checkQuizById(quizId);

		if (quizId != quizUpdated.getQuizId())
			return new ResponseEntity<>(
					"The id missmatch: provided in the path id doesn't equal the id of the quiz in request body",
					HttpStatus.BAD_REQUEST); // 400;

		Long userIdOfQuizInDB = quizInDB.getUser().getId();

		commonService.checkAuthenticationAndRights(auth, userIdOfQuizInDB);

		this.updateQuiz(quizInDB, quizUpdated);

		return new ResponseEntity<>("Quiz info was updated successfully", HttpStatus.OK);
	}

	// Method to publish the quiz by quiz id and authentication instance:
	public ResponseEntity<?> publishQuiz(Long quizId, Authentication auth) {
		Quiz quiz = commonService.checkQuizById(quizId);
		Long idOfQuizOwner = quiz.getUser().getId();

		commonService.checkAuthenticationAndRights(auth, idOfQuizOwner);

		quiz.setStatus("Published");
		quizRepository.save(quiz);

		return new ResponseEntity<>("Quiz was published successfully", HttpStatus.OK);
	}

	// Method to delete quiz by id and authentication instance:
	public ResponseEntity<?> deleteQuizById(Long quizId, Authentication auth) {
		Quiz quiz = commonService.checkQuizById(quizId);
		Long idOfQuizOwner = quiz.getUser().getId();

		commonService.checkAuthenticationAndRights(auth, idOfQuizOwner);

		quizRepository.deleteById(quizId);

		return new ResponseEntity<>("Quiz was deleted successfully", HttpStatus.OK);
	}

	private List<QuizRatingQuestions> makeQuizRatingQuestionsListFromQuizzes(List<Quiz> quizzes) {
		return quizzes.stream().map(this::makeQuizRatingQuestionsFromQuiz).collect(Collectors.toList());
	}

	private QuizRatingQuestions makeQuizRatingQuestionsFromQuiz(Quiz quiz) {
		Long quizId = quiz.getQuizId();

		Double rating = attemptRepository.findQuizRating(quizId);
		Integer questionsAmount = this.getAmountOfQuestionsInQuiz(quizId);

		QuizRatingQuestions quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questionsAmount);

		return quizRatingQuestions;
	}

	private Integer getAmountOfQuestionsInQuiz(Long quizId) {
		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		Integer questionsAmount = questions.size();

		return questionsAmount;
	}

	private List<Quiz> getQuizzesOfOthersThatUserDidntAttempt(Long userId) {
		List<Quiz> quizzesOfOthers = quizRepository.findPublishedQuizzesFromOtherUsers(userId);

		return quizzesOfOthers.stream()
				.filter(quiz -> attemptRepository.findAttemptsForTheQuizByUserId(userId, quiz.getQuizId()) == 0)
				.collect(Collectors.toList());
	}

	private Category findCategoryByName(String categoryName) {
		Optional<Category> optionalCategory = categoryRepository.findByName(categoryName);

		if (!optionalCategory.isPresent())
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Category can't be found by name");

		Category category = optionalCategory.get();

		return category;
	}

	private Category findCategoryById(Long categoryId) {
		Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

		if (!optionalCategory.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category can't be found by ID");

		Category category = optionalCategory.get();

		return category;
	}

	private Difficulty findDifficultyByName(String difficultyName) {
		Optional<Difficulty> optionalDifficulty = difficultyRepository.findByName(difficultyName);

		if (!optionalDifficulty.isPresent())
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Difficulty can't be found by name");

		Difficulty difficulty = optionalDifficulty.get();

		return difficulty;
	}

	private Difficulty findDifficultyById(Long difficultyId) {
		Optional<Difficulty> optionalDifficulty = difficultyRepository.findById(difficultyId);

		if (!optionalDifficulty.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Difficulty can't be found by ID");

		Difficulty difficulty = optionalDifficulty.get();

		return difficulty;
	}

	private ResponseEntity<?> createDefaultQuiz(User user, Category category, Difficulty difficulty) {
		Quiz quiz = new Quiz(user, category, difficulty);
		quizRepository.save(quiz);

		// Creating two default questions with four default answers for each.
		this.createDefaultQuestion(quiz);
		this.createDefaultQuestion(quiz);

		String quizIdString = quiz.getQuizId().toString();

		return ResponseEntity.ok().header(HttpHeaders.HOST, quizIdString)
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
	}

	private void createDefaultQuestion(Quiz quiz) {
		Question defaultQuestion = new Question(quiz);
		questionRepository.save(defaultQuestion);

		this.createDefaultAnswers(defaultQuestion);
	}

	private void createDefaultAnswers(Question question) {
		Answer defaultAnswer;
		boolean isCorrect;

		for (int i = 0; i < 4; i++) {
			isCorrect = (i == 0); // First answer is correct, rest are false
			defaultAnswer = new Answer(question, isCorrect);
			answerRepository.save(defaultAnswer);
		}
	}

	private void updateQuiz(Quiz quizInDB, QuizUpdate quizUpdated) {
		Long categoryId = quizUpdated.getCategory();
		Long difficultyId = quizUpdated.getDifficulty();

		Category category = this.findCategoryById(categoryId);
		Difficulty difficulty = this.findDifficultyById(difficultyId);

		quizInDB.setCategory(category);
		quizInDB.setDifficulty(difficulty);
		quizInDB.setDescription(quizUpdated.getDescription());
		quizInDB.setMinutes(quizUpdated.getMinutes());
		quizInDB.setTitle(quizUpdated.getTitle());
		quizRepository.save(quizInDB);
	}
}
