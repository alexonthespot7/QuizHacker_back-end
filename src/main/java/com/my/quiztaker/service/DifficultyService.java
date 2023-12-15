package com.my.quiztaker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;

@Service
public class DifficultyService {
	@Autowired
	private DifficultyRepository difficultyRepository;

	public List<Difficulty> getDifficulties() {

		return (List<Difficulty>) difficultyRepository.findAll();

	}
}
