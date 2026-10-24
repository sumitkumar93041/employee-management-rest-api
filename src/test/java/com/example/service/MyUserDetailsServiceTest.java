package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.model.Users;
import com.example.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

	@Mock
	private UserRepo userRepo;

	@InjectMocks
	private MyUserDetailsService myUserDetailsService;

	private Users newUser(String username, String password, String role) {
		Users user = new Users();
		user.setUsername(username);
		user.setPassword(password);
		user.setRole(role);
		return user;
	}

	@Test
	void shouldReturnUserDetailsWhenUserExists() {
		when(userRepo.findByUsername("sumit")).thenReturn(newUser("sumit", "hash", "ADMIN"));

		UserDetails result = myUserDetailsService.loadUserByUsername("sumit");

		assertEquals("sumit", result.getUsername());
		assertEquals("hash", result.getPassword());
		assertEquals("ADMIN", result.getAuthorities().iterator().next().getAuthority());

		verify(userRepo).findByUsername("sumit");
	}

	@Test
	void shouldUseRoleFromDatabaseAsAuthority() {
		when(userRepo.findByUsername("testuser")).thenReturn(newUser("testuser", "hash", "USER"));

		UserDetails result = myUserDetailsService.loadUserByUsername("testuser");

		assertEquals(1, result.getAuthorities().size());
		assertEquals("USER", result.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	void shouldThrowWhenUserDoesNotExist() {
		when(userRepo.findByUsername("ghost")).thenReturn(null);

		assertThrows(UsernameNotFoundException.class,
				() -> myUserDetailsService.loadUserByUsername("ghost"));

		verify(userRepo).findByUsername("ghost");
	}
}