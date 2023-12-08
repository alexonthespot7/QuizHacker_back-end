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

import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class DifficultyRepositoryTest {
	@Autowired
	private DifficultyRepository difficultyRepository;

	@BeforeAll
	public void resetRepos() {
		difficultyRepository.deleteAll();
	}

	// CRUD tests for the difficulty repositories

	// Create functionality tests
	@Test
	@Rollback
	public void testCreateDifficulty() {
		Difficulty newDifficulty1 = this.createDifficulty("Hard", 1.0);
		assertThat(newDifficulty1.getDifficultyId()).isNotNull();

		this.createDifficulty("Medium", 0.66);
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).hasSize(2);
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).isEmpty();

		Optional<Difficulty> optionalDifficulty = difficultyRepository.findById(Long.valueOf(2));
		assertThat(optionalDifficulty).isNotPresent();

		Difficulty newDifficulty1 = this.createDifficulty("Hard", 1.0);
		Long newDifficulty1Id = newDifficulty1.getDifficultyId();
		this.createDifficulty("Hard", 1.0);

		difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).hasSize(2);

		optionalDifficulty = difficultyRepository.findById(newDifficulty1Id);
		assertThat(optionalDifficulty).isPresent();
	}

	@Test
	@Rollback
	public void testFindByName() {
		String nameToFindDifficulty = "Hard";
		Optional<Difficulty> optionalDifficulty = difficultyRepository.findByName(nameToFindDifficulty);
		assertThat(optionalDifficulty).isNotPresent();

		this.createDifficulty(nameToFindDifficulty, 1.0);
		optionalDifficulty = difficultyRepository.findByName(nameToFindDifficulty);
		assertThat(optionalDifficulty).isPresent();
	}

	// Testing update functionalities:
	@Test
	@Rollback
	public void testUpdateDifficulty() {
		Difficulty difficulty = this.createDifficulty("Hard", 1.0);
		difficulty.setName("Medium");
		difficulty.setRate(0.66);
		difficultyRepository.save(difficulty);

		Optional<Difficulty> optionalUpdatedDifficulty = difficultyRepository.findByName("Medium");
		assertThat(optionalUpdatedDifficulty).isPresent();

		difficulty = optionalUpdatedDifficulty.get();
		assertThat(difficulty.getName()).isEqualTo("Medium");
		assertThat(difficulty.getRate()).isEqualTo(0.66);
	}

	// Testing delete functionalities:
	@Test
	@Rollback
	public void testDeleteDifficulty() {
		Difficulty difficultyHard = this.createDifficulty("Hard", 1.0);
		Long difficultyHardId = difficultyHard.getDifficultyId();

		difficultyRepository.deleteById(difficultyHardId);
		List<Difficulty> difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).isEmpty();

		this.createDifficulty("Hard", 1.0);
		this.createDifficulty("Medium", 0.66);

		difficultyRepository.deleteAll();
		difficulties = (List<Difficulty>) difficultyRepository.findAll();
		assertThat(difficulties).isEmpty();
	}

	private Difficulty createDifficulty(String name, Double rate) {
		Difficulty newDifficulty = new Difficulty(name, rate);
		difficultyRepository.save(newDifficulty);

		return newDifficulty;
	}
}
