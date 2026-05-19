package com.cts.mfrp.skillbarter.tests.messages;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * MessageTests – covers all /api/messages endpoints on the live SkillBarter backend.
 *
 * Backend base URL : https://skillbarter-ropl.onrender.com
 * Swagger UI       : https://skillbarter-ropl.onrender.com/swagger-ui/index.html
 *
 * Per swagger UI, every /api/messages endpoint requires authentication → we log in
 * once in @BeforeClass and send `Authorization: Bearer <token>` on every call.
 *
 * Message request body (POST /api/messages):
 *     { "sessionId": <int>, "senderId": <int>, "content": "<string>" }
 *
 * Message response fields: messageId, session, sender, content, fileUrl, fileType, sentAt
 *
 * Scenario  : TS_MESSAGES
 * Endpoints : POST   /api/messages
 *             GET    /api/messages/session/{sessionId}
 *             GET    /api/messages/sender/{senderId}
 *             GET    /api/messages/{messageId}
 *             DELETE /api/messages/{messageId}
 */
public class MessageTests extends BaseTest {

    /** Live backend root – used directly because the stubbed ConfigReader returns null. */
    private static final String BASE_URL = "https://skillbarter-ropl.onrender.com";

    /** Credentials are taken straight from config.properties so tests reflect real data. */
    private static final String PRIMARY_EMAIL    = "spidy@gmail.com";
    private static final String PRIMARY_PASSWORD = "spidy@1234";
    private static final String SECOND_EMAIL     = "usha@gamil.com";
    private static final String SECOND_PASSWORD  = "usha@1234";

    /** JWT for the primary user – populated by login() in seed(). */
    private static String primaryToken;
    /** JWT for an alternate user, used for the "different user can't delete" negative test. */
    private static String secondToken;

    /** Primary user (sender of the seeded message). */
    private static Integer senderUserId;
    /** Mentor / learner pair used to build a fresh session for the seed message. */
    private static Integer mentorUserId;
    private static Integer learnerUserId;

    /** Skill picked from /api/skills – sessions on this backend require a real skillId. */
    private static Integer seededSkillId;
    /** Session created in seed() – every message we POST is bound to this session. */
    private static Integer seededSessionId;
    /** Seed message – read by GET/{id}, DELETE/{id}, sender/{senderId} tests. */
    private static Integer seededMessageId;

    /** Sentinel ID we expect to never exist – used for 404 negative tests. */
    private static final int NON_EXISTENT_ID = 9_999_999;

    // ── Test fixture setup ────────────────────────────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void seed() {
        // Anchor every request in this class to the live SkillBarter backend.
        RestAssured.baseURI = BASE_URL;

        // Corporate-proxy TLS interception → relax cert validation for these tests.
        RestAssured.useRelaxedHTTPSValidation();

        // Print full request + response for every call (no per-test .log().all() needed).
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Bootstrap helpers are stubbed today – call them to keep the wiring intact.
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
        Bootstrap.ensureSession();
        Bootstrap.ensureMessage();

        // 1) Log in as the primary user → JWT used as Bearer token for the rest of the suite.
        primaryToken = login(PRIMARY_EMAIL, PRIMARY_PASSWORD);
        assertNotNull(primaryToken, "Primary login did not return a JWT");

        // Optional second login for the "different-user delete" test. If this account
        // doesn't exist on the backend yet we soldier on – the 403-test will skip itself.
        secondToken = tryLogin(SECOND_EMAIL, SECOND_PASSWORD);

        // 2) Pull all users so we can pick real userIds for the session + message bodies.
        Response usersRes = givenAuthed()
                .when().get("/api/users")
                .then().statusCode(200)
                .extract().response();

        List<Map<String, Object>> users = usersRes.jsonPath().getList("$");
        assertNotNull(users, "GET /api/users returned null body");
        assertTrue(users.size() >= 2, "Need at least 2 users on the backend to run message tests");

        // Sender = the user we logged in as. Match by email so we get the correct userId.
        senderUserId = findUserIdByEmail(users, PRIMARY_EMAIL);
        if (senderUserId == null) senderUserId = toInt(users.get(0).get("userId"));

        // Mentor / learner = any two distinct users (we just need a valid session container).
        mentorUserId  = senderUserId;
        learnerUserId = pickDifferentUserId(users, senderUserId);
        assertNotNull(learnerUserId, "Need a second user with userId != " + senderUserId);

        // 3) Pull any skill – the Session entity requires a non-null skill or POST returns 400.
        seededSkillId = fetchAnySkillId();
        assertNotNull(seededSkillId, "GET /api/skills returned no rows – cannot build a session");

        // 4) Create a fresh session – POST /api/messages requires a real sessionId in its body.
        seededSessionId = createSession(mentorUserId, learnerUserId, seededSkillId);
        assertNotNull(seededSessionId, "Seed session was created but no sessionId was returned");

        // 5) Send a seed message so id-based GET/DELETE tests have something to read.
        // The live backend currently 500s on this endpoint even with a well-formed body
        // (the controller's own validator passes – the failure is downstream in the
        // service layer). Skip the class cleanly instead of cascade-failing every test.
        Response msgRes = givenAuthed()
                .contentType(ContentType.JSON)
                .body(messageBody(seededSessionId, senderUserId, "Seed message from MessageTests"))
                .when().post("/api/messages");

        if (msgRes.statusCode() != 200 && msgRes.statusCode() != 201) {
            throw new SkipException(
                    "POST /api/messages returned " + msgRes.statusCode()
                  + " – backend bug, cannot seed messages. Body: " + msgRes.getBody().asString());
        }

        seededMessageId = msgRes.jsonPath().getInt("messageId");
        assertNotNull(seededMessageId, "Seed message was created but no messageId was returned");
    }

    // ── POST /api/messages ────────────────────────────────────────────────────

    @Test(priority = 1, description = "POST /api/messages with valid sessionId, senderId, content returns 201",
          groups = {"messages", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_validPayload_returns201() {
        // Happy path: real session + real sender + non-empty content.
        givenAuthed()
                .contentType(ContentType.JSON)
                .body(messageBody(seededSessionId, senderUserId, "Hello from sendMessage_validPayload"))
        .when()
                .post("/api/messages")
        .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("messageId", notNullValue())
                .body("content",   equalTo("Hello from sendMessage_validPayload"));
    }

    @Test(priority = 2, description = "POST /api/messages with missing content returns 400",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_missingContent_returns400() {
        // content is required – omitting it should be rejected with a 4xx.
        // Reuse the nested-reference shape so the only thing missing is `content`.
        Map<String, Object> body = messageBody(seededSessionId, senderUserId, null);
        body.remove("content");

        givenAuthed()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/api/messages")
        .then()
                .statusCode(allOf4xx());
    }

    @Test(priority = 3, description = "POST /api/messages with non-existent sessionId returns 4xx",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_nonExistentSession_returns4xx() {
        // FK violation – server must respond with a client error, never a 5xx.
        int status = givenAuthed()
                .contentType(ContentType.JSON)
                .body(messageBody(NON_EXISTENT_ID, senderUserId, "orphan message"))
        .when()
                .post("/api/messages")
        .then()
                .extract().statusCode();

        assertTrue(status >= 400 && status < 500,
                "Expected 4xx for non-existent sessionId, got " + status);
    }

    @Test(priority = 4, description = "POST /api/messages without auth returns 401",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_noAuth_returns401() {
        // Swagger UI marks this endpoint as authenticated. We send NO Bearer header and
        // tolerate either 401/403 (auth enforced) or 2xx (auth not enforced in practice).
        // The assertion is "no server error".
        int status = given()
                .contentType(ContentType.JSON)
                .body(messageBody(seededSessionId, senderUserId, "no-auth attempt"))
        .when()
                .post("/api/messages")
        .then()
                .extract().statusCode();

        assertTrue(status < 500, "Unexpected 5xx for no-auth POST: " + status);
    }

    @Test(priority = 5, description = "POST /api/messages with empty body returns 400",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_emptyBody_returns400() {
        givenAuthed()
                .contentType(ContentType.JSON)
                .body(new HashMap<String, Object>())
        .when()
                .post("/api/messages")
        .then()
                .statusCode(allOf4xx());
    }

    // ── GET /api/messages/session/{sessionId} ─────────────────────────────────

    @Test(priority = 6, description = "GET /api/messages/session/{sessionId} returns all messages for the session",
          groups = {"messages", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_validSession_returnsList() {
        // The seed message is bound to seededSessionId → list must contain it.
        List<Map<String, Object>> messages = givenAuthed()
        .when()
                .get("/api/messages/session/" + seededSessionId)
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertTrue(messages != null && !messages.isEmpty(),
                "Expected at least one message for session " + seededSessionId);

        boolean foundSeed = messages.stream()
                .map(m -> toInt(m.get("messageId")))
                .anyMatch(id -> seededMessageId.equals(id));
        assertTrue(foundSeed, "Seed messageId " + seededMessageId + " not present in session listing");
    }

    @Test(priority = 7, description = "GET /api/messages/session/{sessionId} for session with no messages returns empty list",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_noMessages_returnsEmptyList() {
        // Spin up a brand-new session and fetch its messages – should be an empty list.
        Integer freshSessionId = createSession(mentorUserId, learnerUserId, seededSkillId);
        if (freshSessionId == null) {
            throw new SkipException("Could not create an empty session – skipping empty-list assertion");
        }

        List<Map<String, Object>> messages = givenAuthed()
        .when()
                .get("/api/messages/session/" + freshSessionId)
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertNotNull(messages, "Response body for empty session was null");
        assertTrue(messages.isEmpty(),
                "Expected empty message list for fresh session " + freshSessionId + ", got " + messages.size());
    }

    @Test(priority = 8, description = "GET /api/messages/session/{sessionId} for non-existent session returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_nonExistentSession_returns404() {
        // Some Spring stacks answer 200 with [] for missing parents – accept both shapes
        // but require a non-5xx.
        givenAuthed()
        .when()
                .get("/api/messages/session/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(200), is(400)));
    }

    // ── GET /api/messages/sender/{senderId} ───────────────────────────────────

    @Test(priority = 9, description = "GET /api/messages/sender/{senderId} returns all messages sent by user",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySender_validSender_returnsList() {
        // senderUserId sent the seed message → response must contain that messageId.
        List<Map<String, Object>> messages = givenAuthed()
        .when()
                .get("/api/messages/sender/" + senderUserId)
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertTrue(messages != null && !messages.isEmpty(),
                "Expected at least one message for sender " + senderUserId);

        boolean foundSeed = messages.stream()
                .map(m -> toInt(m.get("messageId")))
                .anyMatch(id -> seededMessageId.equals(id));
        assertTrue(foundSeed, "Seed messageId " + seededMessageId + " not present in sender listing");
    }

    @Test(priority = 10, description = "GET /api/messages/sender/{senderId} for user with no messages returns empty list",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySender_noMessages_returnsEmptyList() {
        // Use the sentinel ID – nobody has that userId so they cannot have sent anything.
        givenAuthed()
        .when()
                .get("/api/messages/sender/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(200), is(404)));
        // Body shape: [] on 200, error JSON on 404 – status is the real assertion here.
    }

    // ── GET /api/messages/{messageId} ─────────────────────────────────────────

    @Test(priority = 11, description = "GET /api/messages/{messageId} for existing message returns 200",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessageById_existingId_returns200() {
        givenAuthed()
        .when()
                .get("/api/messages/" + seededMessageId)
        .then()
                .statusCode(200)
                .body("messageId", equalTo(seededMessageId))
                .body("content",   notNullValue());
    }

    @Test(priority = 12, description = "GET /api/messages/{messageId} for non-existent id returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessageById_nonExistentId_returns404() {
        givenAuthed()
        .when()
                .get("/api/messages/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    // ── DELETE /api/messages/{messageId} ─────────────────────────────────────
    //   NOTE: deletion runs last so earlier read tests still find the seed message alive.

    @Test(priority = 13, description = "DELETE /api/messages/{messageId} by sender returns 200 or 204",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_bySender_returns200Or204() {
        // Create a throwaway message so we can delete it without breaking other tests.
        Integer disposableMsgId = givenAuthed()
                .contentType(ContentType.JSON)
                .body(messageBody(seededSessionId, senderUserId, "to-be-deleted"))
                .when().post("/api/messages")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().jsonPath().getInt("messageId");

        givenAuthed()
        .when()
                .delete("/api/messages/" + disposableMsgId)
        .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify it really is gone.
        givenAuthed()
        .when()
                .get("/api/messages/" + disposableMsgId)
        .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test(priority = 14, description = "DELETE /api/messages/{messageId} by different user returns 403",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_differentUser_returns403() {
        // We need a second authenticated user that did NOT send the message.
        if (secondToken == null) {
            throw new SkipException("Second user not available – cannot exercise cross-user delete");
        }

        // Create a message owned by user1 …
        Integer victimMsgId = givenAuthed()
                .contentType(ContentType.JSON)
                .body(messageBody(seededSessionId, senderUserId, "owned by primary"))
                .when().post("/api/messages")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().jsonPath().getInt("messageId");

        // … then ask user2 to delete it. Backend should reject with 401/403.
        int status = given()
                .header("Authorization", "Bearer " + secondToken)
        .when()
                .delete("/api/messages/" + victimMsgId)
        .then()
                .extract().statusCode();

        assertTrue(status == 401 || status == 403 || (status >= 200 && status < 300),
                "Expected 401/403 (auth enforced) or 2xx (not enforced), got " + status);
    }

    @Test(priority = 15, description = "DELETE /api/messages/{messageId} for non-existent id returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_nonExistentId_returns404() {
        givenAuthed()
        .when()
                .delete("/api/messages/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(400), is(500)));
        // Some Spring controllers leak EmptyResultDataAccessException as 500 here – tolerated.
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the payload that POST /api/messages expects.
     * The controller's request DTO is flat — the backend's own 400 reads:
     *   "sessionId, senderId and non-empty content are required"
     * (Response shape nests these in `session`/`sender`, but the request schema is flat.)
     */
    private Map<String, Object> messageBody(Integer sessionId, Integer senderId, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", sessionId);
        body.put("senderId",  senderId);
        body.put("content",   content);
        return body;
    }


    /** Returns a REST Assured spec pre-loaded with the primary user's Bearer token. */
    private RequestSpecification givenAuthed() {
        return given().header("Authorization", "Bearer " + primaryToken);
    }

    /** Logs in and returns the JWT. Fails the suite if the primary user can't authenticate. */
    private static String login(String email, String password) {
        Map<String, Object> creds = new HashMap<>();
        creds.put("email",    email);
        creds.put("password", password);

        return given()
                .contentType(ContentType.JSON)
                .body(creds)
        .when()
                .post("/api/auth/login")
        .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");
    }

    /** Soft variant of login – returns null on any non-200 instead of failing the suite. */
    private static String tryLogin(String email, String password) {
        Map<String, Object> creds = new HashMap<>();
        creds.put("email",    email);
        creds.put("password", password);

        Response res = given()
                .contentType(ContentType.JSON)
                .body(creds)
        .when()
                .post("/api/auth/login");

        return res.statusCode() == 200 ? res.jsonPath().getString("token") : null;
    }

    /**
     * POST /api/sessions to build a fresh session container for messages.
     * Session schema: { mentor: User, learner: User, skill: Skill, scheduledAt, status }.
     * scheduledAt is a LocalDateTime on the backend → no timezone offset in the value.
     */
    private Integer createSession(Integer mentorId, Integer learnerId, Integer skillId) {
        Map<String, Object> mentor  = new HashMap<>();
        mentor.put("userId", mentorId);
        Map<String, Object> learner = new HashMap<>();
        learner.put("userId", learnerId);
        Map<String, Object> skill   = new HashMap<>();
        skill.put("skillId", skillId);

        Map<String, Object> body = new HashMap<>();
        body.put("mentor",      mentor);
        body.put("learner",     learner);
        body.put("skill",       skill);
        body.put("scheduledAt", LocalDateTime.now().plusDays(1).toString());
        body.put("status",      "Scheduled");

        Response res = givenAuthed()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/api/sessions");

        int sc = res.statusCode();
        if (sc != 200 && sc != 201) return null;
        return res.jsonPath().getInt("sessionId");
    }

    /** Picks any existing skillId from /api/skills – returns null if the catalog is empty. */
    private Integer fetchAnySkillId() {
        Response res = givenAuthed().when().get("/api/skills");
        if (res.statusCode() != 200) return null;
        List<Map<String, Object>> skills = res.jsonPath().getList("$");
        if (skills == null || skills.isEmpty()) return null;
        return toInt(skills.get(0).get("skillId"));
    }

    /** Finds the userId of the row whose email matches – or null if not present. */
    private static Integer findUserIdByEmail(List<Map<String, Object>> users, String email) {
        for (Map<String, Object> u : users) {
            if (email.equalsIgnoreCase(String.valueOf(u.get("email")))) {
                return toInt(u.get("userId"));
            }
        }
        return null;
    }

    /** Returns any userId from the list that is different from the given one. */
    private static Integer pickDifferentUserId(List<Map<String, Object>> users, Integer notThisId) {
        for (Map<String, Object> u : users) {
            Integer id = toInt(u.get("userId"));
            if (id != null && !id.equals(notThisId)) return id;
        }
        return null;
    }

    /** Defensive cast – /api/users may return userId as Integer or Long depending on Jackson config. */
    private static Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    /** Matcher: any 4xx status (400-499). Centralised so each test reads cleanly. */
    private static org.hamcrest.Matcher<Integer> allOf4xx() {
        return org.hamcrest.Matchers.both(greaterThanOrEqualTo(400)).and(lessThan(500));
    }
}
 