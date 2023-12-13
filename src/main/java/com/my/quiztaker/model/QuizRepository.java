package com.my.quiztaker.model;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface QuizRepository extends CrudRepository<Quiz, Long> {
	@Query(value="SELECT * FROM quiz WHERE user_id <> ?1 AND status = 'Published'", nativeQuery=true)
	List<Quiz> findPublishedQuizzesFromOtherUsers(Long userId);

	@Query(value="SELECT * FROM quiz WHERE status = 'Published'", nativeQuery=true)
	List<Quiz> findAllPublished();
	
	@Query(value="SELECT * FROM quiz WHERE user_id = ?1", nativeQuery=true)
	List<Quiz> findQuizzesByUserId(Long userId);
}
