package com.example.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = { "ADMIN_USERNAME=testadmin", "ADMIN_PASSWORD=Admin@123" })
@AutoConfigureMockMvc
public class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private String credentials(String username, String password) {
		return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
	}

	private void register(String username, String password) throws Exception {
		mockMvc.perform(post("/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(username, password)))
				.andExpect(status().isOk());
	}

	private String loginAndGetToken(String username, String password) throws Exception {
		return mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials(username, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	void shouldReturn401WhenNoTokenIsSent() throws Exception {
		mockMvc.perform(get("/employees"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturn401WhenTokenIsFake() throws Exception {
		mockMvc.perform(get("/employees").header("Authorization", "Bearer abc"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldRegisterUserWithRoleUserAndNotReturnPassword() throws Exception {
		mockMvc.perform(post("/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials("flow_user_1", "test123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("flow_user_1"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldIgnoreAdminRoleSentInRegisterBody() throws Exception {
		mockMvc.perform(post("/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"flow_sneaky\",\"password\":\"test123\",\"role\":\"ADMIN\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void shouldReturn409WhenRegisteringSameUsernameTwice() throws Exception {
		register("flow_user_2", "test123");

		mockMvc.perform(post("/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials("flow_user_2", "test123")))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldLoginAndUseTokenToReadEmployees() throws Exception {
		register("flow_user_3", "test123");

		String token = loginAndGetToken("flow_user_3", "test123");

		assertFalse(token.isBlank());
		mockMvc.perform(get("/employees").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturn401WhenPasswordIsWrong() throws Exception {
		register("flow_user_4", "test123");

		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials("flow_user_4", "wrong-password")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturn401WhenUserDoesNotExist() throws Exception {
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials("nobody_here", "test123")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldLetAdminCreatedAtStartupLogin() throws Exception {
		String token = loginAndGetToken("testadmin", "Admin@123");

		assertFalse(token.isBlank());
		mockMvc.perform(get("/employees").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void shouldRunFullEmployeeCrudWhenLoggedIn() throws Exception {
		register("flow_user_5", "test123");
		String auth = "Bearer " + loginAndGetToken("flow_user_5", "test123");

		String employee = "{\"id\":9001,\"first_name\":\"Vaibhav\",\"last_name\":\"Gupta\",\"age\":23,"
				+ "\"salary\":50000.0,\"city\":\"Lucknow\",\"state\":\"Uttar Pradesh\"}";
		String updated = "{\"id\":9001,\"first_name\":\"Vaibhav\",\"last_name\":\"Gupta\",\"age\":24,"
				+ "\"salary\":60000.0,\"city\":\"Delhi\",\"state\":\"Delhi\"}";

		mockMvc.perform(post("/employees").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(employee))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/employees").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(employee))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/employees/9001").header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.first_name").value("Vaibhav"))
				.andExpect(jsonPath("$.city").value("Lucknow"));

		mockMvc.perform(put("/employees/9001").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(updated))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city").value("Delhi"));

		mockMvc.perform(delete("/employees/9001").header("Authorization", auth))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/employees/9001").header("Authorization", auth))
				.andExpect(status().isNotFound());
	}
}