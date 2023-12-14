package com.my.quiztaker.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.LeaderboardAuthorized;
import com.my.quiztaker.forms.QuizRating;
import com.my.quiztaker.forms.QuizRatingQuestions;
import com.my.quiztaker.forms.SignupCredentials;
import com.my.quiztaker.forms.UserPublic;
import com.my.quiztaker.model.AttemptRepository;
import com.my.quiztaker.model.Category;
import com.my.quiztaker.model.CategoryRepository;
import com.my.quiztaker.model.Difficulty;
import com.my.quiztaker.model.DifficultyRepository;
import com.my.quiztaker.model.QuestionRepository;
import com.my.quiztaker.model.Quiz;
import com.my.quiztaker.model.QuizRepository;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.service.AuthenticationService;
import com.my.quiztaker.service.CategoryService;
import com.my.quiztaker.service.DifficultyService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@RestController
public class RestPublicController {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private AttemptRepository attRepository;

	@Autowired
	private QuestionRepository questRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private CategoryRepository catRepository;

	@Autowired
	private DifficultyRepository difRepository;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private DifficultyService difficultyService;

	// limit to display only top 10 players:
	private static final int LIMIT = 10;

	@RequestMapping("/categories")
	public @ResponseBody List<Category> getCategories() {
		return categoryService.getCategories();
	}

	@RequestMapping("/difficulties")
	public @ResponseBody List<Difficulty> getDifficulties() {
		return difficultyService.getDifficulties();
	}

	@RequestMapping("/quizzes")
	public @ResponseBody List<QuizRatingQuestions> getQuizzes() {
		List<Quiz> quizzes = (List<Quiz>) quizRepository.findAllPublished();
		Double rating;
		QuizRatingQuestions quizRatingQuestions;
		Integer questions;

		List<QuizRatingQuestions> quizRatingsQuestionsList = new ArrayList<QuizRatingQuestions>();

		for (Quiz quiz : quizzes) {
			rating = attRepository.findQuizRating(quiz.getQuizId());
			questions = questRepository.findQuestionsByQuizId(quiz.getQuizId()).size();
			quizRatingQuestions = new QuizRatingQuestions(quiz, rating, questions);

			quizRatingsQuestionsList.add(quizRatingQuestions);
		}

		return quizRatingsQuestionsList;
	}

	@RequestMapping("/users")
	public @ResponseBody LeaderboardAuthorized getLeaderboardNoAuth() {
		List<UserPublic> leaders = urepository.findLeaderBoard();
		if (leaders.size() > LIMIT) {
			leaders = leaders.subList(0, LIMIT);
		}

		return new LeaderboardAuthorized(leaders, -1);
	}

	@RequestMapping("/quizzes/{quizid}")
	public @ResponseBody QuizRating getQuizById(@PathVariable("quizid") Long quizId) {
		if (quizRepository.findById(quizId).isPresent()) {
			Quiz quiz = quizRepository.findById(quizId).get();
			Double rating = attRepository.findQuizRating(quizId);

			return new QuizRating(quiz, rating);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There's no quiz with this id");
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> getToken(@RequestBody AccountCredentials credentials)
			throws UnsupportedEncodingException, MessagingException {
		Optional<User> userByMail = urepository.findByEmail(credentials.getUsername());

		UsernamePasswordAuthenticationToken creds;

		if (userByMail.isPresent()) {
			User userByMailPresent = userByMail.get();
			if (userByMailPresent.isAccountVerified()) {
				creds = new UsernamePasswordAuthenticationToken(userByMailPresent.getUsername(),
						credentials.getPassword());
			} else {
				String randomCode = RandomStringUtils.random(6);
				userByMailPresent.setVerificationCode(randomCode);
				urepository.save(userByMailPresent);
				try {
					this.sendVerificationEmail(userByMailPresent);
					return ResponseEntity.accepted().header(HttpHeaders.HOST, userByMailPresent.getId().toString())
							.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
				} catch (MailAuthenticationException exc) {
					userByMailPresent.setAccountVerified(true);
					userByMailPresent.setVerificationCode(null);
					urepository.save(userByMailPresent);

					return ResponseEntity.created(null).header(HttpHeaders.HOST, userByMailPresent.getId().toString())
							.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").contentType(MediaType.TEXT_PLAIN)
							.body("The email service is not available now, your account has been verified. You can login now");
				}
			}
		} else {
			if (urepository.findByUsername(credentials.getUsername()).isPresent()) {
				User userByUsername = urepository.findByUsername(credentials.getUsername()).get();
				if (userByUsername.isAccountVerified()) {
					creds = new UsernamePasswordAuthenticationToken(credentials.getUsername(),
							credentials.getPassword());
				} else {
					String randomCode = RandomStringUtils.random(6);
					userByUsername.setVerificationCode(randomCode);
					urepository.save(userByUsername);
					try {
						this.sendVerificationEmail(userByUsername);
						return ResponseEntity.accepted().header(HttpHeaders.HOST, userByUsername.getId().toString())
								.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
					} catch (MailAuthenticationException exc) {
						userByUsername.setAccountVerified(true);
						userByUsername.setVerificationCode(null);
						return ResponseEntity.created(null).header(HttpHeaders.HOST, userByUsername.getId().toString())
								.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host")
								.contentType(MediaType.TEXT_PLAIN)
								.body("The email service is not available now, your account has been verified. You can login now");
					}
				}
			} else {
				return new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);
			}
		}

		Authentication auth = authenticationManager.authenticate(creds);

		String jwts = jwtService.getToken(auth.getName());

		Optional<User> currentUser = urepository.findByUsername(auth.getName());
		if (currentUser.isPresent()) {
			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts)
					.header(HttpHeaders.ALLOW, currentUser.get().getRole())
					.header(HttpHeaders.HOST, currentUser.get().getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Allow", "Host").build();
		} else {
			return new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);
		}

	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@RequestBody SignupCredentials creds)
			throws UnsupportedEncodingException, MessagingException {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(creds.getPassword());
		String randomCode = RandomStringUtils.random(6);

		if (urepository.findByUsername(creds.getUsername()).isPresent()) {
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);
		} else if (urepository.findByEmail(creds.getEmail()).isPresent()) {
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		} else {
			User newUser = new User(creds.getUsername(), hashPwd, "USER", creds.getEmail(), randomCode, false);
			urepository.save(newUser);
			try {
				this.sendVerificationEmail(newUser);
				return ResponseEntity.ok().header(HttpHeaders.HOST, newUser.getId().toString())
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
			} catch (MailAuthenticationException exc) {
				newUser.setAccountVerified(true);
				newUser.setVerificationCode(null);
				return ResponseEntity.created(null).header(HttpHeaders.HOST, newUser.getId().toString())
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").contentType(MediaType.TEXT_PLAIN)
						.body("The email service is not available now, your account has been verified. You can login now");
			}
		}
	}

	@RequestMapping(value = "/verify/{userid}", method = RequestMethod.POST)
	public ResponseEntity<?> verifyUser(@RequestBody String token, @PathVariable("userid") Long userId) {
		if (!urepository.findById(userId).isPresent())
			return new ResponseEntity<>("Wrong user id", HttpStatus.BAD_REQUEST); // 400

		if (!urepository.findById(userId).get().isAccountVerified()) {
			User user = urepository.findById(userId).get();
			if (user.getVerificationCode().equals(token)) {
				user.setVerificationCode(null);
				user.setAccountVerified(true);
				urepository.save(user);
				return new ResponseEntity<>("Verification went well", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Verification code is incorrect", HttpStatus.CONFLICT); // 409
			}
		} else {
			return new ResponseEntity<>("The user is already verified", HttpStatus.BAD_REQUEST); // 400
		}
	}

	@RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
	public ResponseEntity<?> resetPassword(@RequestBody String email)
			throws UnsupportedEncodingException, MessagingException {
		Optional<User> user = urepository.findByEmail(email);

		if (user.isPresent()) {
			User currentUser = user.get();

			if (currentUser.isAccountVerified()) {
				String password = RandomStringUtils.random(15);

				BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
				String hashPwd = bc.encode(password);
				currentUser.setPassword(hashPwd);
				urepository.save(currentUser);

				try {
					this.sendPasswordEmail(currentUser, password);
					return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
				} catch (MailAuthenticationException exc) {
					return new ResponseEntity<>("This service isn't available at the moment",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				return new ResponseEntity<>("User with this email (" + email + ") is not verified",
						HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>("User with this email (" + email + ") doesn't exist", HttpStatus.BAD_REQUEST);
		}
	}

	private void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "aleksei.application.noreply@gmail.com";
		String senderName = "No reply";
		String subject = "QuizHack verification link";
		String content = "Dear [[name]],<br><br>"
				+ "This is the automatically generated message, please don't reply. To verify your account on QuizHack use the verification code below:<br><br>"
				+ "<h3>[[code]]</h3>" + "Thank you,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());

		content = content.replace("[[code]]", user.getVerificationCode());

		helper.setText(content, true);

		mailSender.send(message);
	}

	private void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "aleksei.application.noreply@gmail.com";
		String senderName = "No reply";
		String subject = "Reset password";
		String content = "Dear [[name]],<br><br>"
				+ "Here is your new TEMPORARY password for your QuizHack account:<br><br>" + "<h3>[[PASSWORD]]</h3>"
				+ "Thank you,<br>" + "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());

		content = content.replace("[[PASSWORD]]", password);

		helper.setText(content, true);

		mailSender.send(message);
	}
}
