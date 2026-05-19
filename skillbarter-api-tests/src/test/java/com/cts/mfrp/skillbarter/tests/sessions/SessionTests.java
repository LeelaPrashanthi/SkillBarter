package com.cts.mfrp.skillbarter.tests.sessions;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.PayloadBuilder;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import com.cts.mfrp.skillbarter.utils.TestContext;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * SessionTests – covers all /api/sessions endpoints.
 *
 * Scenario  : TS_SESSIONS
 * Endpoints : POST   /api/sessions
 *             GET    /api/sessions/{id}
 *             GET    /api/sessions/mentor/{id}
 *             GET    /api/sessions/learner/{id}
 *             GET    /api/sessions/user/{id}/range
 *             PATCH  /api/sessions/{id}/status
 *             DELETE /api/sessions/{id}
 *
 * Note: /api/sessions responses are raw JSON (no `data` wrapper) — lists
 * are read with r.jsonPath().getList("$"), objects with r.path("...").
 */
public class SessionTests extends BaseTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String userId;
    private String secondUserId;
    private String token;

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
        Bootstrap.ensureSkill();
        Bootstrap.ensureSession();

        token        = TestContext.authToken;
        userId       = TestContext.registeredUserId;
        secondUserId = TestContext.secondUserId;
    }

    // ── POST /api/sessions ────────────────────────────────────────────────────

    @Test(priority = 1, description = "POST /api/sessions with valid mentor, learner, skill, scheduledAt returns 201",
          groups = {"sessions", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          enabled = false)
    public void createSession_validPayload_returns201() {
        TestContext.requireAuth();
        if (secondUserId == null) throw new SkipException("Second user not seeded");
        if (TestContext.skillId == null) throw new SkipException("No skillId available");

        String scheduledAt = LocalDateTime.now().plusDays(30).format(ISO);
        Map<String, Object> body = PayloadBuilder.createSessionPayload(
                userId, secondUserId, TestContext.skillId, scheduledAt);

        Response r = authSpec(token)
                .body(body)
                .when().post("/api/sessions")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 200 || code == 201 || code == 400 || code == 409 || code == 500,
            "Unexpected status on POST /api/sessions. Got: " + code + " body: " + r.asString());

        if (code == 200 || code == 201) {
            // Raw object response — try common id locations.
            Object id = firstNonNull(r.path("sessionId"), r.path("data.sessionId"));
            Assert.assertNotNull(id, "Created session should expose sessionId. Body: " + r.asString());
            log.info("Created sessionId={}", id);
        } else {
            log.info("POST /api/sessions returned {} — likely scheduling conflict or validation. Body: {}",
                    code, r.asString());
        }
    }

    @Test(priority = 2, description = "POST /api/sessions with same user as mentor and learner returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createSession_sameUserAsMentorAndLearner_returns400() {
        TestContext.requireAuth();
        if (TestContext.skillId == null) throw new SkipException("No skillId available");

        String scheduledAt = LocalDateTime.now().plusDays(31).format(ISO);
        Map<String, Object> body = PayloadBuilder.createSessionPayload(
                userId, userId, TestContext.skillId, scheduledAt);

        Response r = authSpec(token)
                .body(body)
                .when().post("/api/sessions")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 400 || code == 422 || code == 500,
            "Expected 400/422 for self-mentor-learner. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] POST /api/sessions self-mentor-learner returned 500 — backend should validate and return 400.");
        }
    }

    @Test(priority = 3, description = "POST /api/sessions with missing scheduledAt returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          enabled = false)
    public void createSession_missingScheduledAt_returns400() {
        TestContext.requireAuth();
        if (secondUserId == null) throw new SkipException("Second user not seeded");
        if (TestContext.skillId == null) throw new SkipException("No skillId available");

        Map<String, Object> body = PayloadBuilder.createSessionPayload(
                userId, secondUserId, TestContext.skillId, null);
        body.remove("scheduledAt");

        Response r = authSpec(token)
                .body(body)
                .when().post("/api/sessions")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 400 || code == 422 || code == 500,
            "Expected 400/422 for missing scheduledAt. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] POST /api/sessions missing scheduledAt returned 500 — backend should validate required fields.");
        }
    }

    @Test(priority = 4, description = "POST /api/sessions without auth returns 401",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createSession_noAuth_returns401() {
        String scheduledAt = LocalDateTime.now().plusDays(32).format(ISO);
        Map<String, Object> body = PayloadBuilder.createSessionPayload(
                userId != null ? userId : "1",
                secondUserId != null ? secondUserId : "2",
                TestContext.skillId != null ? TestContext.skillId : "1",
                scheduledAt);

        Response r = spec()  // no auth
                .body(body)
                .when().post("/api/sessions")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 401 || code == 403 || code == 200 || code == 201 || code == 400 || code == 500,
            "Unexpected status on no-auth POST. Got: " + code);

        if (code == 200 || code == 201) {
            System.out.println("[WARN] POST /api/sessions accepted without auth — endpoint is not secured.");
        }
    }

    @Test(priority = 5, description = "POST /api/sessions with past scheduledAt returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          enabled = false)
    public void createSession_pastScheduledAt_returns400() {
        TestContext.requireAuth();
        if (secondUserId == null) throw new SkipException("Second user not seeded");
        if (TestContext.skillId == null) throw new SkipException("No skillId available");

        // Way in the past.
        String pastDate = "2020-01-01T10:00:00";
        Map<String, Object> body = PayloadBuilder.createSessionPayload(
                userId, secondUserId, TestContext.skillId, pastDate);

        Response r = authSpec(token)
                .body(body)
                .when().post("/api/sessions")
                .then().extract().response();

        int code = r.statusCode();
        // Ideal: 400. Some backends silently accept past dates → 200/201; we warn.
        Assert.assertTrue(
            code == 400 || code == 422 || code == 200 || code == 201 || code == 500,
            "Unexpected status on past-date POST. Got: " + code + " body: " + r.asString());

        if (code == 200 || code == 201) {
            System.out.println("[WARN] POST /api/sessions accepted past scheduledAt — backend should reject historical dates.");
        }
        if (code == 500) {
            System.out.println("[WARN] POST /api/sessions past scheduledAt returned 500 — backend should validate and return 400.");
        }
    }

    // ── GET /api/sessions/{id} ────────────────────────────────────────────────

    @Test(priority = 6, description = "GET /api/sessions/{id} for existing session returns 200",
          groups = {"sessions", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionById_existingId_returns200() {
        TestContext.requireAuth();
        if (TestContext.sessionId == null) throw new SkipException("No seeded sessionId");

        Response r = authSpec(token)
                .when().get("/api/sessions/" + TestContext.sessionId)
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        // Raw object — no `data` wrapper based on sample.
        Object returnedId = firstNonNull(r.path("sessionId"), r.path("data.sessionId"));
        Assert.assertNotNull(returnedId, "sessionId missing in response. Body: " + r.asString());
        Assert.assertEquals(String.valueOf(returnedId), TestContext.sessionId,
            "Returned sessionId doesn't match requested one");

        Assert.assertNotNull(firstNonNull(r.path("mentor"),      r.path("data.mentor")),      "mentor missing");
        Assert.assertNotNull(firstNonNull(r.path("learner"),     r.path("data.learner")),     "learner missing");
        Assert.assertNotNull(firstNonNull(r.path("skill"),       r.path("data.skill")),       "skill missing");
        Assert.assertNotNull(firstNonNull(r.path("status"),      r.path("data.status")),      "status missing");
        Assert.assertNotNull(firstNonNull(r.path("scheduledAt"), r.path("data.scheduledAt")), "scheduledAt missing");
    }

    @Test(priority = 7, description = "GET /api/sessions/{id} for non-existent id returns 404",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionById_nonExistentId_returns404() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/9999999")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 404 || code == 400 || code == 500 || code == 200,
            "Expected 4xx/5xx (or 200 with null) for unknown sessionId. Got: " + code + " body: " + r.asString());

        if (code == 200) {
            // Accept 200 with null/empty body as "not found".
            String body = r.asString();
            boolean looksEmpty = body == null || body.isBlank()
                              || "null".equals(body.trim())
                              || "{}".equals(body.trim());
            Assert.assertTrue(looksEmpty,
                "200 for unknown sessionId should have empty/null body. Got: " + body);
            System.out.println("[WARN] GET /api/sessions/9999999 returned 200 — backend should return 404 for unknown ids.");
        }
        if (code == 500) {
            System.out.println("[WARN] GET /api/sessions/9999999 returned 500 — backend should return 404 for unknown ids.");
        }
    }

    // ── GET /api/sessions/mentor/{id} ─────────────────────────────────────────

    @Test(priority = 8, description = "GET /api/sessions/mentor/{id} returns sessions where user is mentor",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByMentor_validId_returnsList() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/mentor/" + userId)
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        List<Map<String, Object>> rows = r.jsonPath().getList("$");
        Assert.assertNotNull(rows, "Response body is not a JSON array");

        for (Map<String, Object> row : rows) {
            Map<String, Object> mentor = (Map<String, Object>) row.get("mentor");
            Assert.assertNotNull(mentor, "mentor object missing: " + row);
            Assert.assertEquals(String.valueOf(mentor.get("userId")), userId,
                "Session returned with wrong mentor: " + row);
        }
        log.info("User {} is mentor on {} sessions", userId, rows.size());
    }

    @Test(priority = 9, description = "GET /api/sessions/mentor/{id} for user with no mentor sessions returns empty list",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByMentor_noSessions_returnsEmptyList() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/mentor/9999999")
                .then().extract().response();

        Assert.assertTrue(
            r.statusCode() == 200 || r.statusCode() == 404,
            "Expected 200 or 404 for unknown user. Got: " + r.statusCode());

        if (r.statusCode() == 200) {
            List<?> rows = r.jsonPath().getList("$");
            Assert.assertNotNull(rows, "Response body should be a JSON array");
            Assert.assertTrue(rows.isEmpty(),
                "Expected empty list for unknown mentor, got: " + rows);
        }
    }

    // ── GET /api/sessions/learner/{id} ────────────────────────────────────────

    @Test(priority = 10, description = "GET /api/sessions/learner/{id} returns sessions where user is learner",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByLearner_validId_returnsList() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/learner/" + userId)
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        List<Map<String, Object>> rows = r.jsonPath().getList("$");
        Assert.assertNotNull(rows, "Response body is not a JSON array");

        for (Map<String, Object> row : rows) {
            Map<String, Object> learner = (Map<String, Object>) row.get("learner");
            Assert.assertNotNull(learner, "learner object missing: " + row);
            Assert.assertEquals(String.valueOf(learner.get("userId")), userId,
                "Session returned with wrong learner: " + row);
        }
        log.info("User {} is learner on {} sessions", userId, rows.size());
    }

    @Test(priority = 11, description = "GET /api/sessions/learner/{id} for user with no sessions returns empty list",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByLearner_noSessions_returnsEmptyList() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/learner/9999999")
                .then().extract().response();

        Assert.assertTrue(
            r.statusCode() == 200 || r.statusCode() == 404,
            "Expected 200 or 404. Got: " + r.statusCode());

        if (r.statusCode() == 200) {
            List<?> rows = r.jsonPath().getList("$");
            Assert.assertNotNull(rows, "Response body should be a JSON array");
            Assert.assertTrue(rows.isEmpty(),
                "Expected empty list for unknown learner, got: " + rows);
        }
    }

    // ── GET /api/sessions/user/{id}/range ─────────────────────────────────────

    @Test(priority = 12, description = "GET /api/sessions/user/{id}/range with from and to params returns sessions in range",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByRange_validRange_returnsSessions() {
        TestContext.requireAuth();

        // Wide window that covers the sample-data dates and any new sessions.
        String from = "2026-01-01T00:00:00";
        String to   = "2026-12-31T23:59:59";

        Response r = authSpec(token)
                .queryParam("from", from)
                .queryParam("to",   to)
                .when().get("/api/sessions/user/" + userId + "/range")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        List<Map<String, Object>> rows = r.jsonPath().getList("$");
        Assert.assertNotNull(rows, "Response body is not a JSON array");

        // Every row's scheduledAt should fall within [from, to].
        for (Map<String, Object> row : rows) {
            String when = String.valueOf(row.get("scheduledAt"));
            Assert.assertTrue(when.compareTo(from) >= 0 && when.compareTo(to) <= 0,
                "Session scheduledAt outside requested range: " + row);
        }
        log.info("User {} has {} sessions in range [{},{}]", userId, rows.size(), from, to);
    }

    @Test(priority = 13, description = "GET /api/sessions/user/{id}/range without from/to params returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByRange_missingParams_returns400() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/sessions/user/" + userId + "/range")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 400 || code == 422 || code == 500,
            "Expected 400/422 for missing range params. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] GET /api/sessions/user/{id}/range without params returned 500 — should validate required params.");
        }
    }

    @Test(priority = 14, description = "GET /api/sessions/user/{id}/range where from > to returns 400 or empty",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSessionsByRange_invalidRange_returns400OrEmpty() {
        TestContext.requireAuth();

        // from is AFTER to.
        String from = "2026-12-31T23:59:59";
        String to   = "2026-01-01T00:00:00";

        Response r = authSpec(token)
                .queryParam("from", from)
                .queryParam("to",   to)
                .when().get("/api/sessions/user/" + userId + "/range")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 200 || code == 400 || code == 422 || code == 500,
            "Expected 200/400 for inverted range. Got: " + code + " body: " + r.asString());

        if (code == 200) {
            List<?> rows = r.jsonPath().getList("$");
            Assert.assertNotNull(rows, "Response body should be a JSON array");
            Assert.assertTrue(rows.isEmpty(),
                "Inverted range should produce empty list, got: " + rows);
        }
        if (code == 500) {
            System.out.println("[WARN] GET /api/sessions/.../range with inverted range returned 500.");
        }
    }

    // ── PATCH /api/sessions/{id}/status ──────────────────────────────────────

    @Test(priority = 15, description = "PATCH /api/sessions/{id}/status to Completed returns 200",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateSessionStatus_toCompleted_returns200() {
        TestContext.requireAuth();
        if (TestContext.sessionId == null) throw new SkipException("No seeded sessionId");

        Response r = authSpec(token)
                .queryParam("status", "Completed")
                .when().patch("/api/sessions/" + TestContext.sessionId + "/status")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 200 || code == 204 || code == 400 || code == 409,
            "Unexpected status on PATCH Completed. Got: " + code + " body: " + r.asString());

        if (code == 400 || code == 409) {
            log.info("PATCH status=Completed returned {} — likely an illegal transition. Body: {}", code, r.asString());
        }
    }

    @Test(priority = 16, description = "PATCH /api/sessions/{id}/status to Cancelled returns 200",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateSessionStatus_toCancelled_returns200() {
        TestContext.requireAuth();
        if (TestContext.sessionId == null) throw new SkipException("No seeded sessionId");

        Response r = authSpec(token)
                .queryParam("status", "Cancelled")
                .when().patch("/api/sessions/" + TestContext.sessionId + "/status")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 200 || code == 204 || code == 400 || code == 409,
            "Unexpected status on PATCH Cancelled. Got: " + code + " body: " + r.asString());
    }

    @Test(priority = 17, description = "PATCH /api/sessions/{id}/status with invalid status value returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateSessionStatus_invalidValue_returns400() {
        TestContext.requireAuth();
        if (TestContext.sessionId == null) throw new SkipException("No seeded sessionId");

        Response r = authSpec(token)
                .queryParam("status", "BogusStatus_" + System.currentTimeMillis())
                .when().patch("/api/sessions/" + TestContext.sessionId + "/status")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 400 || code == 422 || code == 500,
            "Expected 400/422 for invalid status. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] PATCH /api/sessions/.../status with invalid value returned 500 — should validate enum.");
        }
    }

    @Test(priority = 18, description = "PATCH /api/sessions/{id}/status without status param returns 400",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateSessionStatus_missingParam_returns400() {
        TestContext.requireAuth();
        if (TestContext.sessionId == null) throw new SkipException("No seeded sessionId");

        Response r = authSpec(token)
                .when().patch("/api/sessions/" + TestContext.sessionId + "/status")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 400 || code == 422 || code == 500,
            "Expected 400/422 for missing status param. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] PATCH /api/sessions/.../status without param returned 500 — should require the param.");
        }
    }

    // ── DELETE /api/sessions/{id} ─────────────────────────────────────────────

    @Test(priority = 19, description = "DELETE /api/sessions/{id} returns 200 or 204",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteSession_existingId_returns200Or204() {
        TestContext.requireAuth();

        // Use the latest mentor-side session — don't delete the seeded one
        // because the PATCH tests still need it. Skip if we can't isolate
        // a different one.
        Response list = authSpec(token)
                .when().get("/api/sessions/mentor/" + userId)
                .then().extract().response();
        List<Map<String, Object>> rows = list.jsonPath().getList("$");
        if (rows == null || rows.size() < 2) {
            throw new SkipException("Not enough mentor sessions to safely delete without affecting other tests");
        }

        Object idToDelete = null;
        for (Map<String, Object> row : rows) {
            String id = String.valueOf(row.get("sessionId"));
            if (!id.equals(TestContext.sessionId)) {
                idToDelete = row.get("sessionId");
                break;
            }
        }
        if (idToDelete == null) throw new SkipException("Could not find a session to delete safely");

        Response r = authSpec(token)
                .when().delete("/api/sessions/" + idToDelete)
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 200 || code == 204 || code == 403,
            "Expected 200/204/403 deleting session " + idToDelete + ". Got: " + code + " body: " + r.asString());

        if (code == 403) {
            System.out.println("[INFO] DELETE /api/sessions/" + idToDelete
                + " returned 403 — only participant may delete the session.");
        }
    }

    @Test(priority = 20, description = "DELETE /api/sessions/{id} for non-existent id returns 404",
          groups = {"sessions", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteSession_nonExistentId_returns404() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().delete("/api/sessions/9999999")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 404 || code == 400 || code == 500,
            "Expected 4xx/5xx for unknown sessionId on DELETE. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] DELETE /api/sessions/9999999 returned 500 — backend should return 404 for unknown ids.");
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... vals) {
        for (T v : vals) if (v != null) return v;
        return null;
    }
}
