package com.my.quiztaker.web;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.PasswordChange;
import com.my.quiztaker.forms.SignupCredentials;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.service.AuthenticationService;

import net.bytebuddy.utility.RandomString;

@RestController
public class UserController {
	@Autowired
	private UserRepository urepository;

	@Autowired
	private AuthenticationService jwtService;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private JavaMailSender mailSender;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> getToken(@RequestBody AccountCredentials credentials) throws UnsupportedEncodingException, MessagingException {
		Optional<User> userByMail = urepository.findByEmail(credentials.getUsername());

		UsernamePasswordAuthenticationToken creds;

		if (userByMail.isPresent()) {
			User userByMailPresent = userByMail.get();
			if (userByMailPresent.isAccountVerified()) {
				creds = new UsernamePasswordAuthenticationToken(userByMailPresent.getUsername(),
						credentials.getPassword());
			} else {
				String randomCode = RandomString.make(6);
				userByMailPresent.setVerificationCode(randomCode);
				urepository.save(userByMailPresent);
				this.sendVerificationEmail(userByMailPresent);
				
				return ResponseEntity.accepted().header(HttpHeaders.HOST, userByMailPresent.getId().toString())
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
			}
		} else {
			if (urepository.findByUsername(credentials.getUsername()).isPresent()) {
				User userByUsername = urepository.findByUsername(credentials.getUsername()).get();
				if (userByUsername.isAccountVerified()) {
					creds = new UsernamePasswordAuthenticationToken(credentials.getUsername(),
							credentials.getPassword());
				} else {
					String randomCode = RandomString.make(6);
					userByUsername.setVerificationCode(randomCode);
					urepository.save(userByUsername);
					this.sendVerificationEmail(userByUsername);
					
					return ResponseEntity.accepted().header(HttpHeaders.HOST, userByUsername.getId().toString())
							.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
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
	public ResponseEntity<?> signUp(@RequestBody SignupCredentials creds, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		String hashPwd = bc.encode(creds.getPassword());
		String randomCode = RandomString.make(6);

		if (urepository.findByUsername(creds.getUsername()).isPresent()) {
			return new ResponseEntity<>("Username is already in use", HttpStatus.CONFLICT);
		} else if (urepository.findByEmail(creds.getEmail()).isPresent()) {
			return new ResponseEntity<>("Email is already in use", HttpStatus.NOT_ACCEPTABLE);
		} else {
			User newUser = new User(creds.getUsername(), hashPwd, "USER", creds.getEmail(), randomCode, false);
			urepository.save(newUser);
			this.sendVerificationEmail(newUser);
			return ResponseEntity.ok().header(HttpHeaders.HOST, newUser.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();
		}
	}

	@RequestMapping(value = "/verify/{userid}", method = RequestMethod.POST)
	public ResponseEntity<?> verifyRequest(@RequestBody String token, @PathVariable("userid") Long userId) {
		if (urepository.findById(userId).isPresent() && !urepository.findById(userId).get().isAccountVerified()) {
			User user = urepository.findById(userId).get();
			if (user.getVerificationCode().equals(token)) {
				user.setVerificationCode(null);
				user.setAccountVerified(true);
				urepository.save(user);
				return new ResponseEntity<>("Verification went well", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Verification code is incorrect", HttpStatus.CONFLICT); //409
			}
		} else {
			return new ResponseEntity<>("Wrong user id or the user is already verified",
					HttpStatus.BAD_REQUEST); //400
		}
	}

	@RequestMapping(value = "/changepassword", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> changePassword(@RequestBody PasswordChange passwordInfo) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		Optional<User> userOptional = urepository.findByUsername(passwordInfo.getUsername());
		Optional<User> optUserByEmail = urepository.findByEmail(passwordInfo.getUsername());
		User user = null;
		boolean flag = false;

		if (userOptional.isPresent()) {
			user = userOptional.get();
			flag = true;
		} else if (optUserByEmail.isPresent()) {
			user = optUserByEmail.get();
			flag = true;
		}
		if (flag) {
			if (bc.matches(passwordInfo.getOldPassword(), user.getPassword())) {
				String hashPwd = bc.encode(passwordInfo.getNewPassword());

				user.setPassword(hashPwd);
				urepository.save(user);

				return new ResponseEntity<>("The password was changed", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("The old password is incorrect", HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>("User with this username or email does not exist", HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
	public ResponseEntity<?> resetPassword(@RequestBody String email)
			throws UnsupportedEncodingException, MessagingException {
		Optional<User> user = urepository.findByEmail(email);

		if (user.isPresent()) {
			User currentUser = user.get();

			if (currentUser.isAccountVerified()) {
				String password = RandomString.make(15);

				BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
				String hashPwd = bc.encode(password);
				currentUser.setPassword(hashPwd);
				urepository.save(currentUser);

				this.sendPasswordEmail(currentUser, password);

				return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
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
