package com.my.quiztaker.model;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AttemptRepository extends CrudRepository<Attempt, Long> {
	@Query(value = "SELECT ROUND(AVG(rating), 2) FROM attempt WHERE quiz_id = ?1", nativeQuery = true)
	Double findQuizRating(Long quizId);
	
	@Query(value = "SELECT COUNT(*) FROM attempt WHERE user_id = ?1", nativeQuery = true)
	Integer findAttemptsByUserId(Long userId);
	
	@Query(value = "SELECT COUNT(*) FROM attempt WHERE user_id = ?1 AND quiz_id = ?2", nativeQuery = true)
	Integer findAttemptsForTheQuizByUserId(Long userId, Long quizId);
}
