package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.model.UserRepositoryAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = { //TODO code smell
		"spring.datasource.url=jdbc:h2:mem:testdb_board;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"app.jwt.secret=test-jwt-secret-at-least-32-characters-long",
		"app.jwt.expiration-seconds=3600",
		"bucket4j.enabled=false"
})
class BoardControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepositoryAdapter userRepositoryAdapter;

	private UUID defaultUserId() {
		return userRepositoryAdapter.findByUsername("anders")
				.orElseThrow(() -> new IllegalStateException("Seed user 'anders' not found")).id();
	}

	private String authHeader() throws Exception {
		return authHeader("anders", "pass");
	}

	private String authHeader(String username, String password) throws Exception {
		String loginPayload = """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);

		MvcResult loginResult = mockMvc
				.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(loginPayload))
				.andExpect(status().isOk()).andReturn();

		String json = loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode node = objectMapper.readTree(json);
		String token = node.get("token").asText();
		return "Bearer " + token;
	}

	@Test
	void getBoardsForUser_shouldReturnUnauthorizedWithoutToken() throws Exception {
		UUID userId = defaultUserId();

		mockMvc.perform(get("/api/boards/user/" + userId)).andExpect(status().isForbidden());
	}

	@Test
	void getBoard_shouldReturnForbiddenForUserThatDoesNotOwnBoard() throws Exception {
		UUID ownerId = defaultUserId();
		String ownerAuth = authHeader();

		// Create a board owned by the default user
		String boardPayload = """
				{
				  "name": "Secured Board",
				  "width": 50.0,
				  "height": 25.0,
				  "userId": "%s"
				}
				""".formatted(ownerId);

		MvcResult createBoardResult = mockMvc
				.perform(post("/api/boards").header("Authorization", ownerAuth)
						.contentType(MediaType.APPLICATION_JSON).content(boardPayload))
				.andExpect(status().isCreated()).andReturn();

		String boardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String boardId = objectMapper.readTree(boardJson).get("id").asText();

		// Register a different user
		String otherUsername = "otheruser_" + System.currentTimeMillis();
		String otherEmail = otherUsername + "@example.com";
		String registerPayload = """
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "password123"
				}
				""".formatted(otherUsername, otherEmail);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(registerPayload))
				.andExpect(status().isCreated());

		// Login as the other user and attempt to access the board
		String otherAuth = authHeader(otherUsername, "password123");

		mockMvc.perform(get("/api/boards/" + boardId).header("Authorization", otherAuth))
				.andExpect(status().isForbidden());
	}

	@Test
	void createBoard_shouldPersistAndReturnBoard() throws Exception {
		UUID userId = defaultUserId();
		String uniqueName = "Test Board " + UUID.randomUUID();
		String payload = String.format("""
				{
				  "name": "%s",
				  "width": 60.0,
				  "height": 30.0,
				  "userId": "%s"
				}
				""", uniqueName, userId);

		mockMvc.perform(post("/api/boards").header("Authorization", authHeader())
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").value(uniqueName))
				.andExpect(jsonPath("$.width").value(60.0)).andExpect(jsonPath("$.height").value(30.0));
	}

	@Test
	void addPedalToBoard_shouldCreatePedalOnBoard() throws Exception {
		// First create a board
		UUID userId = defaultUserId();
		String uniqueName = "Test Board " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 80.0,
				  "height": 40.0,
				  "userId": "%s"
				}
				""", uniqueName, userId);

		MvcResult createBoardResult = mockMvc
				.perform(post("/api/boards").header("Authorization", authHeader())
						.contentType(MediaType.APPLICATION_JSON).content(boardPayload))
				.andExpect(status().isCreated()).andReturn();

		String boardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode boardNode = objectMapper.readTree(boardJson);
		JsonNode idNode = boardNode.get("id");
		String boardId = idNode.isTextual() ? idNode.asText() : idNode.get("value").asText();
		UUID.fromString(boardId); // sanity check parsable UUID

		// Then add a pedal to that board
		String pedalPayload = """
				{
				  "name": "Overdrive",
				  "width": 10.0,
				  "height": 10.0,
				  "color": "#ff0000",
				  "x": 5.0,
				  "y": 5.0,
				  "placement": 1
				}
				""";

		mockMvc.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", authHeader())
				.contentType(MediaType.APPLICATION_JSON).content(pedalPayload)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").value("Overdrive"))
				.andExpect(jsonPath("$.width").value(10.0)).andExpect(jsonPath("$.height").value(10.0))
				.andExpect(jsonPath("$.color").value("#ff0000")).andExpect(jsonPath("$.placement").value(1));
	}

	@Test
	void deleteBoard_shouldRemoveBoardAndReturnNotFoundAfterwards() throws Exception {
		// Create a board
		UUID userId = defaultUserId();
		String uniqueName = "Test Board " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 50.0,
				  "height": 25.0,
				  "userId": "%s"
				}
				""", uniqueName, userId);

		MvcResult createBoardResult = mockMvc
				.perform(post("/api/boards").header("Authorization", authHeader())
						.contentType(MediaType.APPLICATION_JSON).content(boardPayload))
				.andExpect(status().isCreated()).andReturn();

		String boardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode boardNode = objectMapper.readTree(boardJson);
		JsonNode idNode = boardNode.get("id");
		String boardId = idNode.isTextual() ? idNode.asText() : idNode.get("value").asText();

		// Delete the board
		mockMvc.perform(delete("/api/boards/" + boardId).header("Authorization", authHeader()))
				.andExpect(status().isNoContent());

		// Subsequent GET should return 404
		MvcResult getAfterDeleteResult = mockMvc
				.perform(get("/api/boards/" + boardId).header("Authorization", authHeader()))
				.andExpect(status().isNotFound()).andReturn();

		assertThat(getAfterDeleteResult.getResponse().getStatus()).isEqualTo(404);
	}

	@Test
	void createBoard_shouldReturnBadRequest_whenNameAlreadyExists() throws Exception {
		UUID userId = defaultUserId();
		String uniqueName = "Test Board " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 50.0,
				  "height": 25.0,
				  "userId": "%s"
				}
				""", uniqueName, userId);

		// Create first board
		mockMvc.perform(post("/api/boards").header("Authorization", authHeader())
				.contentType(MediaType.APPLICATION_JSON).content(boardPayload)).andExpect(status().isCreated());

		// Try to create another board with the same name
		mockMvc.perform(post("/api/boards").header("Authorization", authHeader())
				.contentType(MediaType.APPLICATION_JSON).content(boardPayload)).andExpect(status().isBadRequest());
	}

	@Test
	void getBoardsForUser_shouldReturnListOfBoardSummaries() throws Exception {
		UUID userId = defaultUserId();
		String uniqueName = "My Boards " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 50.0,
				  "height": 25.0,
				  "userId": "%s"
				}
				""", uniqueName, userId);

		mockMvc.perform(post("/api/boards").header("Authorization", authHeader())
				.contentType(MediaType.APPLICATION_JSON).content(boardPayload)).andExpect(status().isCreated());

		mockMvc.perform(get("/api/boards/user/" + userId).header("Authorization", authHeader()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(greaterThan(0)))
				.andExpect(jsonPath("$[?(@.name=='" + uniqueName + "')].id").exists());
	}

	@Test
	void createCable_shouldCreateCableBetweenTwoPedals() throws Exception {
		UUID userId = defaultUserId();
		String auth = authHeader();
		String boardName = "Cable Board " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 60.0,
				  "height": 40.0,
				  "userId": "%s"
				}
				""", boardName, userId);

		MvcResult createBoardResult = mockMvc.perform(post("/api/boards").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(boardPayload)).andExpect(status().isCreated())
				.andReturn();

		String boardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String boardId = objectMapper.readTree(boardJson).get("id").asText();

		MvcResult pedal1Result = mockMvc.perform(post("/api/boards/" + boardId + "/pedals")
				.header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "name": "PedalA",
						  "width": 8.0,
						  "height": 8.0,
						  "color": "#ff0000",
						  "x": 5.0,
						  "y": 5.0,
						  "placement": 1
						}
						""")).andExpect(status().isCreated()).andReturn();
		String pedal1Id = objectMapper.readTree(pedal1Result.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.get("id").asText();

		MvcResult pedal2Result = mockMvc.perform(post("/api/boards/" + boardId + "/pedals")
				.header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "name": "PedalB",
						  "width": 8.0,
						  "height": 8.0,
						  "color": "#00ff00",
						  "x": 30.0,
						  "y": 20.0,
						  "placement": 2
						}
						""")).andExpect(status().isCreated()).andReturn();
		String pedal2Id = objectMapper.readTree(pedal2Result.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.get("id").asText();

		String cablePayload = String.format("""
				{
				  "sourcePedalId": "%s",
				  "destinationPedalId": "%s"
				}
				""", pedal1Id, pedal2Id);

		mockMvc.perform(post("/api/boards/" + boardId + "/cables").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(cablePayload)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.boardId").value(boardId))
				.andExpect(jsonPath("$.sourcePedalId").value(pedal1Id))
				.andExpect(jsonPath("$.destinationPedalId").value(pedal2Id))
				.andExpect(jsonPath("$.pathPoints").isArray())
				.andExpect(jsonPath("$.pathPoints.length()", greaterThan(0)))
				.andExpect(jsonPath("$.totalLength", greaterThan(0.0)));

		mockMvc.perform(get("/api/boards/" + boardId + "/cables").header("Authorization", auth))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void endToEnd_userCreatesMovesAndDeletesBoard() throws Exception {
		UUID userId = defaultUserId();
		String auth = authHeader();

		// 1) Create a board
		String boardName = "Flow Board " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 70.0,
				  "height": 35.0,
				  "userId": "%s"
				}
				""", boardName, userId);

		MvcResult createBoardResult = mockMvc
				.perform(post("/api/boards").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
						.content(boardPayload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(boardName)).andReturn();

		String createdBoardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode createdBoard = objectMapper.readTree(createdBoardJson);
		String boardId = createdBoard.get("id").asText();

		// 2) Add first pedal
		String pedal1Payload = """
				{
				  "name": "Drive",
				  "width": 10.0,
				  "height": 10.0,
				  "color": "#ff0000",
				  "x": 5.0,
				  "y": 5.0,
				  "placement": 1
				}
				""";

		MvcResult pedal1Result = mockMvc
				.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(pedal1Payload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Drive")).andReturn();

		String pedal1Json = pedal1Result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode pedal1Node = objectMapper.readTree(pedal1Json);
		String pedal1Id = pedal1Node.get("id").asText();

		// 3) Add second pedal
		String pedal2Payload = """
				{
				  "name": "Delay",
				  "width": 12.0,
				  "height": 8.0,
				  "color": "#00ff00",
				  "x": 20.0,
				  "y": 10.0,
				  "placement": 2
				}
				""";

		mockMvc.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(pedal2Payload)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Delay"));

		// 4) Add third pedal
		String pedal3Payload = """
				{
				  "name": "Chorus",
				  "width": 9.0,
				  "height": 9.0,
				  "color": "#0000ff",
				  "x": 10.0,
				  "y": 20.0,
				  "placement": 3
				}
				""";

		MvcResult pedal3Result = mockMvc
				.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(pedal3Payload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Chorus")).andReturn();

		String pedal3Json = pedal3Result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode pedal3Node = objectMapper.readTree(pedal3Json);
		String pedal3Id = pedal3Node.get("id").asText();

		// 5) Verify board now has 3 pedals
		mockMvc.perform(get("/api/boards/" + boardId).header("Authorization", auth)).andExpect(status().isOk())
				.andExpect(jsonPath("$.pedals.length()").value(3));

		// 6) Move first pedal
		String movePayload = """
				{
				  "x": 30.0,
				  "y": 15.0
				}
				""";

		mockMvc.perform(put("/api/pedals/" + pedal1Id).header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(movePayload)).andExpect(status().isOk())
				.andExpect(jsonPath("$.x").value(30.0)).andExpect(jsonPath("$.y").value(15.0));

		// 7) Move third pedal
		String movePayload3 = """
				{
				  "x": 40.0,
				  "y": 5.0
				}
				""";

		mockMvc.perform(put("/api/pedals/" + pedal3Id).header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(movePayload3)).andExpect(status().isOk())
				.andExpect(jsonPath("$.x").value(40.0)).andExpect(jsonPath("$.y").value(5.0));

		// 8) Verify board reflects updated position for moved pedals
		mockMvc.perform(get("/api/boards/" + boardId).header("Authorization", auth)).andExpect(status().isOk())
				.andExpect(jsonPath("$.pedals[?(@.id=='" + pedal1Id + "')].x").value(30.0))
				.andExpect(jsonPath("$.pedals[?(@.id=='" + pedal1Id + "')].y").value(15.0))
				.andExpect(jsonPath("$.pedals[?(@.id=='" + pedal3Id + "')].x").value(40.0))
				.andExpect(jsonPath("$.pedals[?(@.id=='" + pedal3Id + "')].y").value(5.0));

		// 9) Generate cables and verify they exist with length
		mockMvc.perform(post("/api/boards/" + boardId + "/generate-sequence").header("Authorization", auth))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/boards/" + boardId + "/cables").header("Authorization", auth))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThan(0)))
				.andExpect(jsonPath("$[0].totalLength", greaterThan(0.0)));

		// 10) Delete the board
		mockMvc.perform(delete("/api/boards/" + boardId).header("Authorization", auth))
				.andExpect(status().isNoContent());

		// 11) Subsequent GET should be 404
		mockMvc.perform(get("/api/boards/" + boardId).header("Authorization", auth)).andExpect(status().isNotFound());
	}

	@Test
	void deletePedal_useCase_onePedalLeftAndNoCables() throws Exception {
		String auth = authHeader();
		UUID userId = defaultUserId();

		// 1) Create a board
		String boardName = "Delete Pedal Test " + UUID.randomUUID();
		String boardPayload = String.format("""
				{
				  "name": "%s",
				  "width": 60.0,
				  "height": 30.0,
				  "userId": "%s"
				}
				""", boardName, userId);

		MvcResult createBoardResult = mockMvc
				.perform(post("/api/boards").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
						.content(boardPayload))
				.andExpect(status().isCreated()).andReturn();

		String boardJson = createBoardResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		String boardId = objectMapper.readTree(boardJson).get("id").asText();

		// 2) Create pedal A (placement 1)
		String pedalAPayload = """
				{
				  "name": "Pedal A",
				  "width": 10.0,
				  "height": 10.0,
				  "color": "#ff0000",
				  "x": 5.0,
				  "y": 5.0,
				  "placement": 1
				}
				""";

		MvcResult pedalAResult = mockMvc
				.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON).content(pedalAPayload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.placement").value(1)).andReturn();

		String pedalAId = objectMapper.readTree(pedalAResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
				.get("id").asText();

		// 3) Create pedal B (placement 2)
		String pedalBPayload = """
				{
				  "name": "Pedal B",
				  "width": 10.0,
				  "height": 10.0,
				  "color": "#00ff00",
				  "x": 25.0,
				  "y": 10.0,
				  "placement": 2
				}
				""";

		mockMvc.perform(post("/api/boards/" + boardId + "/pedals").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(pedalBPayload))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.placement").value(2));

		// 4) Move one pedal to a new position
		String movePayload = """
				{
				  "x": 15.0,
				  "y": 12.0
				}
				""";

		mockMvc.perform(put("/api/pedals/" + pedalAId).header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(movePayload)).andExpect(status().isOk());

		// 5) Connect pedals (generate sequence) so a cable exists
		mockMvc.perform(post("/api/boards/" + boardId + "/generate-sequence").header("Authorization", auth))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/boards/" + boardId + "/cables").header("Authorization", auth))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThan(0)));

		// 6) Delete pedal A (pedal 1)
		mockMvc.perform(delete("/api/pedals/" + pedalAId).header("Authorization", auth))
				.andExpect(status().isNoContent());

		// 7) Verify one pedal is left
		mockMvc.perform(get("/api/boards/" + boardId).header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pedals.length()").value(1))
				.andExpect(jsonPath("$.pedals[0].name").value("Pedal B"));

		// 8) Verify no cables are left
		mockMvc.perform(get("/api/boards/" + boardId + "/cables").header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}
}
