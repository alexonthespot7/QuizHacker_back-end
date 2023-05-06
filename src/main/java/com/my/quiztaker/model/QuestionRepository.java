package com.my.quiztaker.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<Question, Long> {
	
	@Query(value="SELECT * FROM question WHERE quiz_id = ?1", nativeQuery=true)
	List<Question> findQuestionsByQuizId(Long quizId);
}
