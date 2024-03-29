package com.my.quiztaker.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.my.quiztaker.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	@Autowired
	private CommonService commonService;

	@Autowired
	private JavaMailSender mailSender;

	public ResponseEntity<?> tryToSendVerificationMail(User user)
			throws MessagingException, UnsupportedEncodingException {
		try {
			this.sendVerificationEmail(user);

			return ResponseEntity.accepted().header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").build();

		} catch (MailAuthenticationException exc) {
			commonService.verifyUser(user);

			return ResponseEntity.created(null).header(HttpHeaders.HOST, user.getId().toString())
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Host").contentType(MediaType.TEXT_PLAIN)
					.body("The email service is not available now, your account has been verified. You can login now");
		}
	}

	public ResponseEntity<?> tryToSendPasswordMail(User user, String password)
			throws MessagingException, UnsupportedEncodingException {
		try {
			this.sendPasswordEmail(user, password);
			return new ResponseEntity<>("A temporary password was sent to your email address", HttpStatus.OK);
		} catch (MailAuthenticationException exc) {
			return new ResponseEntity<>("This service isn't available at the moment", HttpStatus.INTERNAL_SERVER_ERROR);
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
