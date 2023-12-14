package com.my.quiztaker.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.Leaderboard;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	// The method returns the list of TOP-10 users in leaderboard (username,
	// rating). The position of current user is -1, what means the user is not in
	// the leaderboard
	public Leaderboard getLeaderboardNoAuth() {
		List<UserPublic> leaders = userRepository.findLeaderBoard();

		leaders = this.getTop10(leaders);

		Leaderboard leaderboard = new Leaderboard(leaders, -1);

		return leaderboard;
	}

	private List<UserPublic> getTop10(List<UserPublic> leaders) {
		if (leaders.size() > LIMIT) {
			leaders = leaders.subList(0, LIMIT);
		}

		return leaders;
	}

	public ResponseEntity<?> getToken(AccountCredentials credentials)
			throws MessagingException, UnsupportedEncodingException {
		String usernameOrEmail = credentials.getUsername();
		String password = credentials.getPassword();

		User user = this.findUserByUsernameOrEmail(usernameOrEmail);

		if (!user.isAccountVerified()) {
			return this.handleUnverifiedUser(user);
		}

		String username = user.getUsername();

		User authenticatedUser = this.authenticateUser(username, password);

		String jwts = jwtService.getToken(username);

		return this.sendResponseWithToken(authenticatedUser, jwts);

	}

	private User authenticateUser(String username, String password) {
		UsernamePasswordAuthenticationToken creds = new UsernamePasswordAuthenticationToken(username, password);

		Authentication auth = authenticationManager.authenticate(creds);

		String authenticatedUsername = auth.getName();

		Optional<User> optionalAuthenticatedUser = userRepository.findByUsername(authenticatedUsername);

		if (!optionalAuthenticatedUser.isPresent())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");

		User authenticatedUser = optionalAuthenticatedUser.get();

		return authenticatedUser;
	}

	private ResponseEntity<?> sendResponseWithToken(User user, String jwts) {
		String role = user.getRole();
		String stringUserId = user.getId().toString();

		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts).header(HttpHeaders.ALLOW, role)
				.header(HttpHeaders.HOST, stringUserId)
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host").build();
	}

	private ResponseEntity<?> handleUnverifiedUser(User user) throws MessagingException, UnsupportedEncodingException {
		this.setVerificationCode(user);

		try {
			mailService.sendVerificationEmail(user);

			return ResponseEntity.accepted().header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();

		} catch (MailAuthenticationException exc) {
			this.verifyUser(user);

			return ResponseEntity.created(null).header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").contentType(MediaType.TEXT_PLAIN)
					.body("The email service is not available now, your account has been verified. You can login now");

		}
	}

	private void setVerificationCode(User user) {
		String randomCode = RandomStringUtils.random(6);
		user.setVerificationCode(randomCode);
		userRepository.save(user);
	}

	private void verifyUser(User user) {
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		userRepository.save(user);
	}

	private User findUserByUsernameOrEmail(String usernameOrEmail) {
		Optional<User> optionalUser = userRepository.findByEmail(usernameOrEmail);

		if (!optionalUser.isPresent()) {
			optionalUser = userRepository.findByUsername(usernameOrEmail);
			if (!optionalUser.isPresent())
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"User wasn't found for provided username or email");
		}

		User user = optionalUser.get();

		return user;
	}
}
