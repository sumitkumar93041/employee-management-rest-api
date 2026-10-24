package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Users;
import com.example.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepo userRepo;

	@Mock
	private AuthenticationManager authManager;

	@Mock
	private JWTService jwtService;

	@InjectMocks
	private UserService userService;

	private Users newUser(String username, String password) {
		Users user = new Users();
		user.setUsername(username);
		user.setPassword(password);
		return user;
	}

	@Test
	void shouldSaveUserWithBcryptPasswordWhenRegistering() {
		Users request = newUser("testuser", "test123");
		when(userRepo.findByUsername("testuser")).thenReturn(null);
		when(userRepo.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

		userService.register(request);

		ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
		verify(userRepo).save(captor.capture());
		Users saved = captor.getValue();

		assertEquals("testuser", saved.getUsername());
		assertNotEquals("test123", saved.getPassword());
		assertTrue(saved.getPassword().startsWith("$2"));
		assertTrue(new BCryptPasswordEncoder(10).matches("test123", saved.getPassword()));
	}

	@Test
	void shouldForceRoleUserEvenWhenRequestSaysAdmin() {
		Users request = newUser("sneaky", "test123");
		request.setRole("ADMIN");
		when(userRepo.findByUsername("sneaky")).thenReturn(null);
		when(userRepo.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

		userService.register(request);

		ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
		verify(userRepo).save(captor.capture());
		assertEquals("USER", captor.getValue().getRole());
	}

	@Test
	void shouldRejectRegisterWhenUsernameAlreadyExists() {
		Users existing = newUser("testuser", "hash");
		when(userRepo.findByUsername("testuser")).thenReturn(existing);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.register(newUser("testuser", "test123")));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(userRepo, never()).save(any(Users.class));
	}

	@Test
	void shouldCreateAdminWhenAdminDoesNotExist() {
		when(userRepo.findByUsername("admin")).thenReturn(null);

		userService.createAdminIfMissing("admin", "Admin@123");

		ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
		verify(userRepo).save(captor.capture());
		Users saved = captor.getValue();

		assertEquals("admin", saved.getUsername());
		assertEquals("ADMIN", saved.getRole());
		assertTrue(new BCryptPasswordEncoder(10).matches("Admin@123", saved.getPassword()));
	}

	@Test
	void shouldNotCreateAdminWhenAdminAlreadyExists() {
		when(userRepo.findByUsername("admin")).thenReturn(newUser("admin", "hash"));

		userService.createAdminIfMissing("admin", "Admin@123");

		verify(userRepo, never()).save(any(Users.class));
	}

	@Test
	void shouldReturnTokenWhenCredentialsAreValid() {
		Authentication authenticated =
				new UsernamePasswordAuthenticationToken("testuser", "test123", List.of());
		when(authManager.authenticate(any(Authentication.class))).thenReturn(authenticated);
		when(jwtService.generateToken("testuser")).thenReturn("fake.jwt.token");

		String token = userService.verify(newUser("testuser", "test123"));

		assertEquals("fake.jwt.token", token);

		ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
		verify(authManager).authenticate(captor.capture());
		assertEquals("testuser", captor.getValue().getName());
		assertEquals("test123", captor.getValue().getCredentials());

		verify(jwtService).generateToken("testuser");
	}

	@Test
	void shouldReturnFailWhenAuthenticationIsNotAuthenticated() {
		Authentication notAuthenticated = new UsernamePasswordAuthenticationToken("testuser", "wrong");
		when(authManager.authenticate(any(Authentication.class))).thenReturn(notAuthenticated);

		String result = userService.verify(newUser("testuser", "wrong"));

		assertEquals("fail", result);
		verify(jwtService, never()).generateToken(any(String.class));
	}

	@Test
	void shouldThrowAndNotCreateTokenWhenCredentialsAreWrong() {
		when(authManager.authenticate(any(Authentication.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		assertThrows(BadCredentialsException.class,
				() -> userService.verify(newUser("testuser", "wrong")));

		verify(jwtService, never()).generateToken(any(String.class));
	}
}