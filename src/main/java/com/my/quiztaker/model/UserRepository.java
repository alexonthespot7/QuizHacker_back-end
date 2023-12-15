package com.my.quiztaker.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.my.quiztaker.forms.UserPublic;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	Optional<User> findByUsername(String username);

	@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
	Optional<User> findByEmail(String email);

	@Query(value = "SELECT * FROM users WHERE verification_code = ?1", nativeQuery = true)
	Optional<User> findByVerificationCode(String code);

	@Query(value = "SELECT username, SUM(att.score * dif.rate) AS rating FROM users "
			+ "JOIN attempt AS att ON (att.user_id = users.id) JOIN quiz ON (quiz.quiz_id = att.quiz_id) "
			+ "JOIN difficulty AS dif ON (dif.difficulty_id = quiz.difficulty_id) "
			+ "GROUP BY username ORDER BY SUM(att.score * dif.rate) DESC, username ASC", nativeQuery = true)
	List<UserPublic> findLeaderBoard();

	@Query(value = "SELECT username, SUM(att.score * dif.rate) AS rating FROM users "
			+ "JOIN attempt AS att ON (att.user_id = users.id) JOIN quiz ON (quiz.quiz_id = att.quiz_id) "
			+ "JOIN difficulty AS dif ON (dif.difficulty_id = quiz.difficulty_id) "
			+ "WHERE id = ?1 GROUP BY username", nativeQuery = true)
	UserPublic findRatingByUserId(Long userId);
}
