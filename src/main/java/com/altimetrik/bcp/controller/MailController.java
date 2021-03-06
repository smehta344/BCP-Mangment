package com.altimetrik.bcp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altimetrik.bcp.mail.MailService;
import com.altimetrik.bcp.model.Mail;

@RestController
@CrossOrigin
@RequestMapping("/mail")
public class MailController {
	
	@Autowired
	MailService mailService;

	@PostMapping(value = "/sendEmail")
	public ResponseEntity<String> createDailyStatus(@RequestBody Mail mail) {
		mailService.sendEmail(mail);
		return ResponseEntity.ok().body("success");
	}
}
