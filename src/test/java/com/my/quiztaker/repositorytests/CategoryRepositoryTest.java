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

import jakarta.transaction.Transactional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class CategoryRepositoryTest {
	@Autowired
	private CategoryRepository categoryRepository;

	@BeforeAll
	public void resetCategoryRepo() {
		categoryRepository.deleteAll();
	}

	// CRUD tests for the category repositories

	// Create functionality tests
	@Test
	@Rollback
	public void testCreateCategory() {
		Category newCategory1 = this.createCategory("Other");
		assertThat(newCategory1.getCategoryId()).isNotNull();

		this.createCategory("IT");
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).hasSize(2);
	}

	// Read functionalities tests
	@Test
	@Rollback
	public void testFindAllAndFindById() {
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).isEmpty();

		Optional<Category> optionalCategory = categoryRepository.findById(Long.valueOf(2));
		assertThat(optionalCategory).isNotPresent();

		Category newCategory1 = this.createCategory("Other");
		Long newCategory1Id = newCategory1.getCategoryId();
		this.createCategory("IT");

		categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).hasSize(2);

		optionalCategory = categoryRepository.findById(newCategory1Id);
		assertThat(optionalCategory).isPresent();
	}

	@Test
	@Rollback
	public void testFindByName() {
		String nameToFindCategory = "Other";
		Optional<Category> optionalCategory = categoryRepository.findByName(nameToFindCategory);
		assertThat(optionalCategory).isNotPresent();

		this.createCategory(nameToFindCategory);
		optionalCategory = categoryRepository.findByName(nameToFindCategory);
		assertThat(optionalCategory).isPresent();
	}

	// Testing update functionalities:
	@Test
	@Rollback
	public void testUpdateCategory() {
		Category category = this.createCategory("Other");
		category.setName("IT");
		categoryRepository.save(category);

		Optional<Category> optionalUpdatedCategory = categoryRepository.findByName("IT");
		assertThat(optionalUpdatedCategory).isPresent();

		category = optionalUpdatedCategory.get();
		assertThat(category.getName()).isEqualTo("IT");
	}

	// Testing delete functionalities:
	@Test
	@Rollback
	public void testDeleteCategory() {
		Category categoryOther = this.createCategory("Other");
		Long categoryOtherId = categoryOther.getCategoryId();
		
		categoryRepository.deleteById(categoryOtherId);
		List<Category> categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).hasSize(0);
		
		this.createCategory("Other");
		this.createCategory("IT");
		
		categoryRepository.deleteAll();
		categories = (List<Category>) categoryRepository.findAll();
		assertThat(categories).hasSize(0);
	}

	private Category createCategory(String name) {
		Category newCategory1 = new Category(name);
		categoryRepository.save(newCategory1);

		return newCategory1;
	}
}
