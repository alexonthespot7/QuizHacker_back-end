package com.my.quiztaker.web;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
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

import org.apache.commons.lang3.RandomStringUtils;

import com.my.quiztaker.forms.AccountCredentials;
import com.my.quiztaker.forms.PasswordChange;
import com.my.quiztaker.forms.SignupCredentials;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.service.AuthenticationService;

@RestController
public class UserController {
	@Autowired
	private UserRepository urepository;

	@Autowired
	AuthenticationManager authenticationManager;

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

	

}
