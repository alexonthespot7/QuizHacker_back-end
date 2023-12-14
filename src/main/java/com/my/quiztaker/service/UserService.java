package com.my.quiztaker.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.Leaderboard;
import com.my.quiztaker.forms.PersonalInfo;
import com.my.quiztaker.forms.SignupCredentials;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AttemptRepository attemptRepository;

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

	// Login method
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

	// Sign up method
	public ResponseEntity<?> signUp(SignupCredentials creds) throws UnsupportedEncodingException, MessagingException {
		String email = creds.getEmail();
		String username = creds.getUsername();
		String password = creds.getPassword();

		this.checkEmailAndUsername(email, username);

		User newUser = this.createUnverifiedUser(email, username, password);

		return mailService.tryToSendVerificationMail(newUser);
	}

	// Verify user method
	public ResponseEntity<?> verifyUser(String token, Long userId) {
		User user = this.findUserById(userId);

		if (user.isAccountVerified())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is already verified");

		if (!user.getVerificationCode().equals(token))
			return new ResponseEntity<>("Verification code is incorrect", HttpStatus.CONFLICT); // 409

		mailService.verifyUser(user);

		return new ResponseEntity<>("Verification went well", HttpStatus.OK);
	}

	// Reset password method for user:
	public ResponseEntity<?> resetPassword(String email) throws UnsupportedEncodingException, MessagingException {
		User user = this.findUserByEmail(email);

		if (!user.isAccountVerified())
			return new ResponseEntity<>("User with this email (" + email + ") is not verified",
					HttpStatus.UNAUTHORIZED);

		String password = this.setNewRandomPassword(user);

		return mailService.tryToSendPasswordMail(user, password);
	}

	// Method to get personal info of authenticated user:
	public PersonalInfo getPersonalInfo(Long userId, Authentication auth) {
		User user = this.checkAuthentication(auth);

		if (user.getId() != userId)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"You are not allowed to get someone else's info");

		return this.createPersonalInfoInstance(user, userId);
	}

	private List<UserPublic> getTop10(List<UserPublic> leaders) {
		if (leaders.size() > LIMIT) {
			leaders = leaders.subList(0, LIMIT);
		}

		return leaders;
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

	private ResponseEntity<?> handleUnverifiedUser(User user) throws MessagingException, UnsupportedEncodingException {
		this.setVerificationCode(user);

		return mailService.tryToSendVerificationMail(user);
	}

	private void setVerificationCode(User user) {
		String randomCode = RandomStringUtils.random(6);
		user.setVerificationCode(randomCode);
		userRepository.save(user);
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

	private void checkEmailAndUsername(String email, String username) {
		this.checkEmail(email);
		this.checkUsername(username);
	}

	private void checkEmail(String email) {
		Optional<User> optionalUserByEmail = userRepository.findByEmail(email);

		if (optionalUserByEmail.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Email is already in use");
	}

	private void checkUsername(String username) {
		Optional<User> optionalUserByUsername = userRepository.findByUsername(username);

		if (optionalUserByUsername.isPresent())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
	}

	private User createUnverifiedUser(String email, String username, String password) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);
		String randomCode = RandomStringUtils.random(6);

		User newUser = new User(username, hashPwd, "USER", email, randomCode, false);
		userRepository.save(newUser);

		return newUser;
	}

	private User findUserById(Long userId) {
		Optional<User> optionalUser = userRepository.findById(userId);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong user id");

		User user = optionalUser.get();

		return user;
	}

	private User findUserByEmail(String email) {
		Optional<User> optionalUser = userRepository.findByEmail(email);

		if (!optionalUser.isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"User with this email (" + email + ") doesn't exist");

		User user = optionalUser.get();
		return user;
	}

	private String setNewRandomPassword(User user) {
		String password = RandomStringUtils.random(15);

		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(password);
		user.setPassword(hashPwd);
		userRepository.save(user);

		return password;
	}

	private User checkAuthentication(Authentication auth) {
		if (!auth.getPrincipal().getClass().toString().equals("class com.my.quiztaker.MyUser"))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");

		MyUser myUser = (MyUser) auth.getPrincipal();
		Optional<User> optionalUser = userRepository.findByUsername(myUser.getUsername());

		if (!optionalUser.isPresent()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
		}

		User user = optionalUser.get();

		return user;
	}

	private PersonalInfo createPersonalInfoInstance(User user, Long userId) {
		UserPublic userPublic = userRepository.findRatingByUserId(userId);

		// if user doesn't have any attempts, they are not in the leaderboard and have
		// rating 0:
		int position = -1;
		String username = user.getUsername();
		String email = user.getEmail();
		Integer userRating = 0;
		Integer attemptsAmount = attemptRepository.findAttemptsByUserId(userId);

		if (userPublic != null) {
			List<UserPublic> leaders = userRepository.findLeaderBoard();
			position = this.findPosition(username, leaders);
			userRating = userPublic.getRating();
		}
		
		PersonalInfo personalInfo = new PersonalInfo(username, email, userRating, attemptsAmount, position);

		return personalInfo;
	}

	private int findPosition(String username, List<UserPublic> leaders) {
		int position = -1;
		UserPublic userPublic;
		String usernameOfCurrentUser;

		for (int i = 0; i < leaders.size(); i++) {
			userPublic = leaders.get(i);
			usernameOfCurrentUser = userPublic.getUsername();

			if (usernameOfCurrentUser.equals(username)) {
				position = i + 1;
				break;
			}
		}

		return position;
	}
}
