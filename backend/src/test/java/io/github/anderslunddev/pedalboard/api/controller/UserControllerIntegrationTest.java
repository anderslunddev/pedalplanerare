package io.github.anderslunddev.pedalboard.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = { //TODO code smell
		"spring.datasource.url=jdbc:h2:mem:testdb_user;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"app.jwt.secret=test-jwt-secret-at-least-32-characters-long",
		"app.jwt.expiration-seconds=3600",
		"bucket4j.enabled=false"
})
class UserControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void register_shouldReturnCreatedUserWithIdAndUsernameAndEmail() throws Exception {
		String username = "newuser_" + System.currentTimeMillis();
		String email = username + "@example.com";
		String payload = String.format("""
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "password123"
				}
				""", username, email);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.username").value(username)).andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void register_shouldReturnBadRequest_whenUsernameAlreadyTaken() throws Exception {
		String username = "duplicate_" + System.currentTimeMillis();
		String payload = String.format("""
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "password123"
				}
				""", username, username + "@example.com");

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Username is already taken"));
	}

	@Test
	void register_shouldReturnBadRequest_whenEmailAlreadyInUse() throws Exception {
		String email = "sameemail_" + System.currentTimeMillis() + "@example.com";
		String firstPayload = String.format("""
				{
				  "username": "first_%d",
				  "email": "%s",
				  "password": "password123"
				}
				""", System.currentTimeMillis(), email);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(firstPayload))
				.andExpect(status().isCreated());

		String secondPayload = String.format("""
				{
				  "username": "second_%d",
				  "email": "%s",
				  "password": "password123"
				}
				""", System.currentTimeMillis(), email);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(secondPayload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Email is already in use"));
	}
}
