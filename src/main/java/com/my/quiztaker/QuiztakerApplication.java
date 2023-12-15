package com.my.quiztaker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@SpringBootApplication
public class QuiztakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuiztakerApplication.class, args);
	}

	@Bean
	public CommandLineRunner tournamentDemo(DifficultyRepository difficultyRepository,
			CategoryRepository categoryRepository, UserRepository userRepository) {
		return (args) -> {
			Difficulty hardDifficulty = new Difficulty("Hard", 3);
			Difficulty mediumDifficulty = new Difficulty("Medium", 2);
			Difficulty smallDifficulty = new Difficulty("Easy", 1);
			difficultyRepository.save(hardDifficulty);
			difficultyRepository.save(mediumDifficulty);
			difficultyRepository.save(smallDifficulty);
			
			Category otherCategory = new Category("Other");
			Category itCategory = new Category("IT");
			categoryRepository.save(otherCategory);
			categoryRepository.save(itCategory);
			
			User user1 = new User("user1", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER", "user1@mail.com", null, true);
			userRepository.save(user1);
			
			User user2 = new User("user2", "$2a$12$YV/TXHTDhWz5Y41p7X6WfegbFVF4/chhCzYtbXKm0gHxltazlbDoe", "USER", "user2@mail.com", null, true);
			userRepository.save(user2);
			
		};
	}

}
