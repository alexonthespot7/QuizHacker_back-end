package com.my.quiztaker.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private JavaMailSender mailSender;

	public void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
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

	public void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
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
	
	public ResponseEntity<?> tryToSendVerificationMail(User user)
			throws MessagingException, UnsupportedEncodingException {
		try {
			this.sendVerificationEmail(user);

			return ResponseEntity.accepted().header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();

		} catch (MailAuthenticationException exc) {
			this.verifyUser(user);

			return ResponseEntity.created(null).header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").contentType(MediaType.TEXT_PLAIN)
					.body("The email service is not available now, your account has been verified. You can login now");
		}
	}
	
	public void verifyUser(User user) {
		user.setAccountVerified(true);
		user.setVerificationCode(null);
		userRepository.save(user);
	}
}
