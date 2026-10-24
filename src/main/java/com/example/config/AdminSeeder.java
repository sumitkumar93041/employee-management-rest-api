package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.service.UserService;


@Component
public class AdminSeeder implements CommandLineRunner {

	@Autowired
	private UserService service;

	@Value("${ADMIN_USERNAME:admin}")
	private String adminUsername;

	@Value("${ADMIN_PASSWORD:}")
	private String adminPassword;

	@Override
	public void run(String... args) {
		if (adminPassword.isBlank()) {
			System.out.println("ADMIN_PASSWORD is not set, so no admin user was created.");
			return;
		}
		service.createAdminIfMissing(adminUsername, adminPassword);
	}
}
