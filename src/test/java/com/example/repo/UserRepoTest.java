package com.example.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.model.Users;

@DataJpaTest
public class UserRepoTest {

	@Autowired
	private UserRepo userRepo;

	private Users newUser(String username, String password, String role) {
		Users user = new Users();
		user.setUsername(username);
		user.setPassword(password);
		user.setRole(role);
		return user;
	}

	@Test
	void shouldGenerateIdWhenSavingUser() {
		Users saved = userRepo.save(newUser("sumit", "hash", "USER"));

		assertTrue(saved.getId() > 0);
	}

	@Test
	void shouldFindUserByUsername() {
		userRepo.save(newUser("sumit", "hash", "ADMIN"));

		Users found = userRepo.findByUsername("sumit");

		assertNotNull(found);
		assertEquals("sumit", found.getUsername());
		assertEquals("hash", found.getPassword());
		assertEquals("ADMIN", found.getRole());
	}

	@Test
	void shouldReturnNullWhenUsernameNotFound() {
		assertNull(userRepo.findByUsername("ghost"));
	}

	@Test
	void shouldRejectDuplicateUsername() {
		userRepo.saveAndFlush(newUser("sumit", "hash1", "USER"));

		assertThrows(DataIntegrityViolationException.class,
				() -> userRepo.saveAndFlush(newUser("sumit", "hash2", "USER")));
	}
}