package io.github.anderslunddev.pedalboard.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/integration-test-reset-and-admin.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private String adminBearer() throws Exception {
		MvcResult loginResult = mockMvc
				.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("""
						{ "username": "testadmin", "password": "password123" }
						"""))
				.andExpect(status().isOk()).andReturn();
		return "Bearer " + JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
	}

	@Test
	void createUser_shouldReturnCreated() throws Exception {
		String username = "newuser_" + System.currentTimeMillis();
		String email = username + "@example.com";
		String payload = String.format("""
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "password123",
				  "role": "USER"
				}
				""", username, email);

		mockMvc.perform(post("/api/admin/users").header("Authorization", adminBearer())
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value(username)).andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void createUser_shouldReturnBadRequest_whenUsernameAlreadyTaken() throws Exception {
		String username = "dup_" + System.currentTimeMillis();
		String payload = String.format("""
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "password123",
				  "role": "USER"
				}
				""", username, username + "@example.com");

		mockMvc.perform(post("/api/admin/users").header("Authorization", adminBearer())
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated());

		mockMvc.perform(post("/api/admin/users").header("Authorization", adminBearer())
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Username is already taken"));
	}

	@Test
	void createUser_shouldReturnBadRequest_whenEmailAlreadyInUse() throws Exception {
		String email = "shared_" + System.currentTimeMillis() + "@example.com";
		String first = String.format("""
				{
				  "username": "first_%d",
				  "email": "%s",
				  "password": "password123",
				  "role": "USER"
				}
				""", System.currentTimeMillis(), email);
		String second = String.format("""
				{
				  "username": "second_%d",
				  "email": "%s",
				  "password": "password123",
				  "role": "USER"
				}
				""", System.currentTimeMillis(), email);

		mockMvc.perform(post("/api/admin/users").header("Authorization", adminBearer())
				.contentType(MediaType.APPLICATION_JSON).content(first)).andExpect(status().isCreated());

		mockMvc.perform(post("/api/admin/users").header("Authorization", adminBearer())
				.contentType(MediaType.APPLICATION_JSON).content(second)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Email is already in use"));
	}
}
