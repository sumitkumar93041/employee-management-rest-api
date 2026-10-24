package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Users;
import com.example.repo.UserRepo;

@Service
public class UserService {

	@Autowired
	public UserRepo repo;

	@Autowired
	AuthenticationManager authManager;
	@Autowired
	private JWTService jwtService;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

	public Users register(Users user) {
		if (repo.findByUsername(user.getUsername()) != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}
		user.setPassword(encoder.encode(user.getPassword()));
		user.setRole("USER");
		return repo.save(user);
	}

	
	public void createAdminIfMissing(String username, String rawPassword) {
		if (repo.findByUsername(username) == null) {
			Users admin = new Users();
			admin.setUsername(username);
			admin.setPassword(encoder.encode(rawPassword));
			admin.setRole("ADMIN");
			repo.save(admin);
		}
	}

	public String verify(Users user) {
		org.springframework.security.core.Authentication authentication =
				authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

		if (authentication.isAuthenticated()) {
			return jwtService.generateToken(user.getUsername());
		}
		return "fail";
	}
}
