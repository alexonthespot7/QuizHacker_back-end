package com.my.quiztaker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;

@Service
public class CategoryService {
	@Autowired
	private CategoryRepository categoryRepository;

	public List<Category> getCategories() {
		
		return (List<Category>) categoryRepository.findAll();
	
	}

}
