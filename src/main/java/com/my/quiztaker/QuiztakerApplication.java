package com.my.quiztaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

@SpringBootApplication
public class QuiztakerApplication {
	@Autowired
	private UserRepository uRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private QuestionRepository questRepository;

	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private CategoryRepository catRepository;
	
	@Autowired 
	private DifficultyRepository difRepository;
	
	@Autowired
	private AttemptRepository attRepository;

	public static void main(String[] args) {
		SpringApplication.run(QuiztakerApplication.class, args);
	}

	/**@Bean
	public CommandLineRunner runner() {
		return (args) -> {
			uRepository.save(new User("user", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail@gmail.com", true));
			uRepository.save(new User("admin", "$2a$12$cSL2mimF9753d0enA5rhHOeTTKlP5rQApiqyahMJrEp9mCX.uqbqG", "ADMIN",
					"mymail2@gmail.com", true));
			uRepository.save(new User("user2", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail232@gmail.com", true));
			uRepository.save(new User("user3", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail23222@gmail.com", true));
			uRepository.save(new User("user4", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail234222@gmail.com", true));
			uRepository.save(new User("user5", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail522@gmail.com", true));
			uRepository.save(new User("user6", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail2362@gmail.com", true));
			uRepository.save(new User("user7", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail27@gmail.com", true));
			uRepository.save(new User("user8", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail82@gmail.com", true));
			uRepository.save(new User("user9", "$2a$12$gzpMx041jG.7q6ynVoBQi.xFdVsImTjzdJQz.xqgSrLPJim7hSec2", "USER",
					"mymail290@gmail.com", true));
			
			catRepository.save(new Category("General Knowledge"));
			catRepository.save(new Category("Science"));
			catRepository.save(new Category("History"));
			catRepository.save(new Category("Literature"));
			catRepository.save(new Category("Math"));
			catRepository.save(new Category("Geography"));
			catRepository.save(new Category("Sports"));
			catRepository.save(new Category("Music"));
			catRepository.save(new Category("Art"));
			catRepository.save(new Category("Technology"));
			catRepository.save(new Category("Pop Culture"));
			catRepository.save(new Category("Languages"));
			catRepository.save(new Category("Politics"));
			catRepository.save(new Category("Food and Drink"));
			catRepository.save(new Category("Religion"));
			catRepository.save(new Category("Mythology"));
			catRepository.save(new Category("Movies and TV"));
			catRepository.save(new Category("Animals"));
			catRepository.save(new Category("Business"));
			catRepository.save(new Category("Environment"));
			catRepository.save(new Category("Other"));
			
			difRepository.save(new Difficulty("Easy", 0.33));
			difRepository.save(new Difficulty("Medium", 0.66));
			difRepository.save(new Difficulty("Hard", 1.0));


			Quiz quiz1 = new Quiz("Countries", catRepository.findByName("Geography").get(), difRepository.findByName("Easy").get(), uRepository.findByUsername("user").get(), 2,
					"Published");
			Quiz quiz2 = new Quiz("Great Britain Cities", "The quiz about cities and towns of Great Britain",
					catRepository.findByName("Geography").get(), difRepository.findByName("Hard").get(), uRepository.findByUsername("admin").get(), 2, "Published");

			quizRepository.save(quiz1);
			quizRepository.save(quiz2);

			Question quest1 = new Question("What is the capital of Great Britain", quiz1);
			Question quest2 = new Question("What is the capital of Spain", quiz1);
			Question quest3 = new Question("What is the capital of Finland", quiz1);
			Question quest4 = new Question("What is the capital of Sweden", quiz1);
			Question quest5 = new Question("What is the capital of Ukraine", quiz1);

			Question quest6 = new Question("Which of the following city is the city of Great Britain", quiz2);
			Question quest7 = new Question("In which country of Great Britain does city of Manchester located", quiz2);
			Question quest8 = new Question("What is the most populated city in Great Britain excluding London", quiz2);
			Question quest9 = new Question("What is the largest city by area in Great Britain excluding London", quiz2);

			questRepository.save(quest1);
			questRepository.save(quest2);
			questRepository.save(quest3);
			questRepository.save(quest4);
			questRepository.save(quest5);
			questRepository.save(quest6);
			questRepository.save(quest7);
			questRepository.save(quest8);
			questRepository.save(quest9);

			Answer answ1 = new Answer("London", true, quest1);
			Answer answ2 = new Answer("Leicester", false, quest1);
			Answer answ3 = new Answer("Liverpool", false, quest1);
			Answer answ4 = new Answer("York", false, quest1);
			
			Answer answ5 = new Answer("Madrid", true, quest2);
			Answer answ6 = new Answer("Barcelona", false, quest2);
			Answer answ7 = new Answer("Sevilla", false, quest2);
			Answer answ8 = new Answer("Granada", false, quest2);
			
			Answer answ9 = new Answer("Helsinki", true, quest3);
			Answer answ10 = new Answer("Turku", false, quest3);
			Answer answ11 = new Answer("Oulu", false, quest3);
			Answer answ12 = new Answer("Espoo", false, quest3);
			
			Answer answ13 = new Answer("Stockholm", true, quest4);
			Answer answ14 = new Answer("Turku", false, quest4);
			Answer answ15 = new Answer("Oslo", false, quest4);
			Answer answ16 = new Answer("Helsinki", false, quest4);
			
			Answer answ17 = new Answer("Kiev", true, quest5);
			Answer answ18 = new Answer("Chernygov", false, quest5);
			Answer answ19 = new Answer("Odessa", false, quest5);
			Answer answ20 = new Answer("Chornobyl", false, quest5);
			
			Answer answ21 = new Answer("New-York", false, quest6);
			Answer answ22 = new Answer("Cardiff", true, quest6);
			Answer answ23 = new Answer("Dublin", false, quest6);
			Answer answ24 = new Answer("Moscow", false, quest6);
			
			Answer answ25 = new Answer("England", true, quest7);
			Answer answ26 = new Answer("Wales", false, quest7);
			Answer answ27 = new Answer("Northern Ireland", false, quest7);
			Answer answ28 = new Answer("Scotland", false, quest7);
			
			Answer answ29 = new Answer("Liverpool", false, quest8);
			Answer answ30 = new Answer("Birmingham", true, quest8);
			Answer answ31 = new Answer("Cardiff", false, quest8);
			Answer answ32 = new Answer("Sheffield", false, quest8);
			
			Answer answ33 = new Answer("City of Carlisle", false, quest9);
			Answer answ34 = new Answer("Birmingham", false, quest9);
			Answer answ35 = new Answer("Liverpool", true, quest9);
			Answer answ36 = new Answer("Manchester", false, quest9);
			
			answerRepository.save(answ1);
			answerRepository.save(answ2);
			answerRepository.save(answ3);
			answerRepository.save(answ4);

			answerRepository.save(answ5);
			answerRepository.save(answ6);
			answerRepository.save(answ7);
			answerRepository.save(answ8);

			answerRepository.save(answ9);
			answerRepository.save(answ10);
			answerRepository.save(answ11);
			answerRepository.save(answ12);

			answerRepository.save(answ13);
			answerRepository.save(answ14);
			answerRepository.save(answ15);
			answerRepository.save(answ16);
			
			answerRepository.save(answ17);
			answerRepository.save(answ18);
			answerRepository.save(answ19);
			answerRepository.save(answ20);

			answerRepository.save(answ21);
			answerRepository.save(answ22);
			answerRepository.save(answ23);
			answerRepository.save(answ24);
			
			answerRepository.save(answ25);
			answerRepository.save(answ26);
			answerRepository.save(answ27);
			answerRepository.save(answ28);
			
			answerRepository.save(answ29);
			answerRepository.save(answ30);
			answerRepository.save(answ31);
			answerRepository.save(answ32);
			
			answerRepository.save(answ33);
			answerRepository.save(answ34);
			answerRepository.save(answ35);
			answerRepository.save(answ36);
			
			Attempt attempt1 = new Attempt(4, quiz1, uRepository.findByUsername("user").get(), 5);
			Attempt attempt2 = new Attempt(5, quiz2, uRepository.findByUsername("user").get(), 5);

			Attempt attempt5 = new Attempt(7, quiz1, uRepository.findByUsername("admin").get(), 1);
			Attempt attempt6 = new Attempt(7, quiz2, uRepository.findByUsername("admin").get(), 2);
			
			Attempt attempt7 = new Attempt(4, quiz1, uRepository.findByUsername("user2").get(), 5);
			Attempt attempt8 = new Attempt(3, quiz2, uRepository.findByUsername("user2").get(), 5);

			Attempt attempt9 = new Attempt(5, quiz1, uRepository.findByUsername("user3").get(), 1);
			Attempt attempt10 = new Attempt(5, quiz2, uRepository.findByUsername("user3").get(), 2);
			
			Attempt attempt11 = new Attempt(2, quiz1, uRepository.findByUsername("user4").get(), 5);
			Attempt attempt12 = new Attempt(3, quiz2, uRepository.findByUsername("user4").get(), 4);
			
			attRepository.save(attempt1);
			attRepository.save(attempt2);

			attRepository.save(attempt5);
			attRepository.save(attempt6);
			
			attRepository.save(attempt7);
			attRepository.save(attempt8);

			attRepository.save(attempt9);
			attRepository.save(attempt10);
			
			attRepository.save(attempt11);
			attRepository.save(attempt12);
		};
	}*/

}
