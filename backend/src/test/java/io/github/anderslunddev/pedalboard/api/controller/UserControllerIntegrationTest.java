package io.github.anderslunddev.pedalboard.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/integration-test-reset-and-admin.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void postUsers_shouldBeForbidden_whenPublicRegistrationDisabled() throws Exception {
		String payload = """
				{
				  "username": "hacker",
				  "email": "hacker@example.com",
				  "password": "password123"
				}
				""";
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isForbidden());
	}

	@Test
	void me_shouldBeDenied_whenNoToken() throws Exception {
		mockMvc.perform(get("/api/users/me")).andExpect(status().isForbidden());
	}

	@Test
	void me_shouldReturnCurrentUser_whenBearerTokenValid() throws Exception {
		MvcResult loginResult = mockMvc
				.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("""
						{ "username": "testadmin", "password": "password123" }
						"""))
				.andExpect(status().isOk()).andReturn();

		String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

		mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("testadmin")).andExpect(jsonPath("$.email").value("admin@test.com"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void changePassword_shouldReturnBadRequest_whenCurrentPasswordWrong() throws Exception {
		String token = loginAsTestAdmin();

		mockMvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{ "currentPassword": "wrong", "newPassword": "newpass999" }
						""")).andExpect(status().isBadRequest());
	}

	@Test
	void changePassword_shouldSucceed_andAllowLoginWithNewPassword() throws Exception {
		String token = loginAsTestAdmin();

		mockMvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{ "currentPassword": "password123", "newPassword": "newpass999" }
						""")).andExpect(status().isNoContent());

		mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("""
				{ "username": "testadmin", "password": "newpass999" }
				""")).andExpect(status().isOk());
	}

	private String loginAsTestAdmin() throws Exception {
		MvcResult loginResult = mockMvc
				.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("""
						{ "username": "testadmin", "password": "password123" }
						"""))
				.andExpect(status().isOk()).andReturn();
		return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
	}
}
