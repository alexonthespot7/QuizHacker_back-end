package com.my.quiztaker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.model.Answer;
import com.my.quiztaker.model.Question;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@Service
public class QuestionService {
	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CommonService commonService;

	// Method to get list of questions for the quiz: if the user is authenticated
	// and is the quiz's owner they can see correct answers, otherwise they cannot
	public List<Question> getQuestionsByQuizId(Long quizId, Authentication auth) {
		Quiz quiz = commonService.checkQuizById(quizId);

		List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);

		if (this.isAuthenticatedAndQuizOwner(quizId, auth, quiz))
			return questions;

		return this.hideCorrectAnswers(questions);
	}

	private boolean isAuthenticatedAndQuizOwner(Long quizId, Authentication auth, Quiz quiz) {
		if (auth == null || !(auth.getPrincipal() instanceof MyUser)) {
	        return false;
	    }

	    MyUser myUser = (MyUser) auth.getPrincipal();
	    Optional<User> optionalUser = userRepository.findByUsername(myUser.getUsername());

	    return optionalUser.map(user -> user.getId().equals(quiz.getUser().getId())).orElse(false);
	}

	private List<Question> hideCorrectAnswers(List<Question> questions) {
	    questions.forEach(question -> question.getAnswers().forEach(answer -> answer.setCorrect(false)));
	    return questions;
	}	
}
