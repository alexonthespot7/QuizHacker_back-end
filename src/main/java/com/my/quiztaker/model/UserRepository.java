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

	@Query(value = "SELECT username, ROUND(SUM(att.score * dif.rate), 2) AS rating\r\n" + "FROM users\r\n"
			+ "JOIN attempt AS att ON (att.user_id = users.id)\r\n" + "JOIN quiz ON (quiz.quiz_id = att.quiz_id)\r\n"
			+ "JOIN difficulty AS dif ON (dif.difficulty_id = quiz.difficulty_id)\r\n"
			+ "GROUP BY username ORDER BY SUM(att.score * dif.rate) DESC LIMIT 10", nativeQuery = true)
	List<UserPublic> findLeaderBoard();

	@Query(value = "SELECT (COUNT(*) + 1) AS position\r\n"
			+ "FROM (\r\n"
			+ "  SELECT users.id, SUM(att.score * dif.rate) AS total_score\r\n"
			+ "  FROM users\r\n"
			+ "  JOIN attempt AS att ON att.user_id = users.id\r\n"
			+ "  JOIN quiz ON quiz.quiz_id = att.quiz_id\r\n"
			+ "  JOIN difficulty AS dif ON dif.difficulty_id = quiz.difficulty_id\r\n"
			+ "  GROUP BY users.id\r\n"
			+ ") AS user_scores\r\n"
			+ "WHERE user_scores.total_score > (\r\n"
			+ "  SELECT SUM(att.score * dif.rate)\r\n"
			+ "  FROM attempt AS att\r\n"
			+ "  JOIN quiz ON quiz.quiz_id = att.quiz_id\r\n"
			+ "  JOIN difficulty AS dif ON dif.difficulty_id = quiz.difficulty_id\r\n"
			+ "  WHERE att.user_id = ?1\r\n"
			+ ");", nativeQuery = true)
	Integer findPositionByRating(Long userId);
	
	@Query(value = "SELECT username, ROUND(SUM(att.score * dif.rate), 2) AS rating FROM users "
			+ "JOIN attempt AS att ON (att.user_id = users.id) JOIN quiz ON (quiz.quiz_id = att.quiz_id) "
			+ "JOIN difficulty AS dif ON (dif.difficulty_id = quiz.difficulty_id) "
			+ "WHERE id = ?1", nativeQuery=true)
	UserPublic findRatingByUserId(Long userId);
}
