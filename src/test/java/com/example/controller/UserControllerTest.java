package com.example.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Users;
import com.example.service.JWTService;
import com.example.service.UserService;

@WebMvcTest(UserController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JWTService jwtService;

	private static final String CREDENTIALS_JSON = "{\"username\":\"testuser\",\"password\":\"test123\"}";

	@Test
	void shouldRegisterUserAndNotReturnPassword() throws Exception {
		Users saved = new Users();
		saved.setId(1);
		saved.setUsername("testuser");
		saved.setPassword("$2a$10$somebcrypthash");
		saved.setRole("USER");
		when(userService.register(any(Users.class))).thenReturn(saved);

		mockMvc.perform(post("/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(CREDENTIALS_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.username").value("testuser"))
			.andExpect(jsonPath("$.role").value("USER"))
			.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldPassUsernameAndPasswordFromBodyToService() throws Exception {
		when(userService.register(any(Users.class))).thenReturn(new Users());

		mockMvc.perform(post("/register")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(CREDENTIALS_JSON));

		ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
		verify(userService).register(captor.capture());
		assertEquals("testuser", captor.getValue().getUsername());
		assertEquals("test123", captor.getValue().getPassword());
	}

	@Test
	void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
		when(userService.register(any(Users.class)))
			.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

		mockMvc.perform(post("/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(CREDENTIALS_JSON))
			.andExpect(status().isConflict());
	}

	@Test
	void shouldReturnTokenWhenLoginSucceeds() throws Exception {
		when(userService.verify(any(Users.class))).thenReturn("fake.jwt.token");

		mockMvc.perform(post("/login")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(CREDENTIALS_JSON))
			.andExpect(status().isOk())
			.andExpect(content().string("fake.jwt.token"));

		verify(userService).verify(any(Users.class));
	}
}