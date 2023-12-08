package com.my.quiztaker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;

@SpringBootApplication
public class QuiztakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuiztakerApplication.class, args);
	}

	@Bean
	public CommandLineRunner tournamentDemo(DifficultyRepository difficultyRepository,
			CategoryRepository categoryRepository) {
		return (args) -> {
			Difficulty hardDifficulty = new Difficulty("Hard", 1.0);
			Difficulty mediumDifficulty = new Difficulty("Medium", 0.66);
			Difficulty smallDifficulty = new Difficulty("Small", 0.33);
			difficultyRepository.save(hardDifficulty);
			difficultyRepository.save(mediumDifficulty);
			difficultyRepository.save(smallDifficulty);
			
			Category otherCategory = new Category("Other");
			Category itCategory = new Category("IT");
			categoryRepository.save(otherCategory);
			categoryRepository.save(itCategory);
		};
	}

}
