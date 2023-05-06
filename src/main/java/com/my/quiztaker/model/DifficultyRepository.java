package com.my.quiztaker.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface DifficultyRepository extends CrudRepository<Difficulty, Long>{
	Optional<Difficulty> findByName(String name);
}
