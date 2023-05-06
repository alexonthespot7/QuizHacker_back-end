package com.my.quiztaker.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AnswerRepository extends CrudRepository<Answer, Long> {
	@Query(value="SELECT * FROM answer WHERE question_id = ?1", nativeQuery=true)
	List<Answer> findByQuestionId(Long questionId);
	
	@Query(value="SELECT * FROM answer WHERE is_correct AND question_id = ?1", nativeQuery = true)
	Answer findCorrectByQuestionId(Long questionId);
	
	@Query(value="SELECT text FROM answer WHERE question_id = ?1", nativeQuery=true)
	List<String> findAnswerTextsByQuestionId(Long questionId);
}
