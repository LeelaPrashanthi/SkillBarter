package com.cts.mfrp.skillbarter.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bootstrap – pre-test data seeding helpers.
 *
 * Each ensure* method is idempotent: it registers / logs in the required
 * user (or captures the required reference id) once and stores the result
 * in TestContext for later tests to consume.
 */
public class Bootstrap {

    private static final Logger log = LogManager.getLogger(Bootstrap.class);

    static {
        // Trust all certs in seeding calls too — see BaseTest.initSpec().
        RestAssured.useRelaxedHTTPSValidation();
    }

    // ── Users ────────────────────────────────────────────────────────────────

    /** Registers (or logs in) the primary test user and stores token + id in TestContext. */
    public static synchronized void ensureFirstUser() {
        if (TestContext.authToken != null && TestContext.registeredUserId != null) return;

        String email    = ConfigReader.getTestEmail();
        String password = ConfigReader.getTestPassword();

        // Try register first — server may return 4xx if email already exists, which is fine.
        try {
            Response reg = RestAssured.given().relaxedHTTPSValidation()
                    .baseUri(ConfigReader.getBaseUrl())
                    .contentType(ContentType.JSON)
                    .body(PayloadBuilder.registerPayload("API Test User", email, password))
                    .when()
                    .post("/api/auth/register")
                    .then().extract().response();
            log.info("REGISTER {} → status {}", email, reg.statusCode());
        } catch (Exception e) {
            log.warn("REGISTER threw: {}", e.getMessage());
        }

        // Now log in to obtain a token.
        Response loginResp = RestAssured.given().relaxedHTTPSValidation()
                .baseUri(ConfigReader.getBaseUrl())
                .contentType(ContentType.JSON)
                .body(PayloadBuilder.loginPayload(email, password))
                .when()
                .post("/api/auth/login")
                .then().extract().response();

        log.info("LOGIN status={}", loginResp.statusCode());

        if (loginResp.statusCode() < 200 || loginResp.statusCode() >= 300) {
            log.warn("Login failed — tests will skip. Check that user '{}' exists on {}",
                    email, ConfigReader.getBaseUrl());
            return;
        }

        // Token / userId may live at various paths depending on backend shape.
        String token = firstNonNull(
                loginResp.path("data.token"),
                loginResp.path("token"),
                loginResp.path("data.accessToken"),
                loginResp.path("accessToken"),
                loginResp.path("jwt"),
                loginResp.path("data.jwt"));
        Object uid = firstNonNull(
                loginResp.path("data.user.userId"),
                loginResp.path("data.userId"),
                loginResp.path("user.userId"),
                loginResp.path("userId"),
                loginResp.path("data.user.id"),
                loginResp.path("data.id"),
                loginResp.path("user.id"),
                loginResp.path("id"));

        TestContext.authToken = token;

        // Login response often only contains the token (e.g. {"token":"..."}). If so,
        // look up the userId via a "me" endpoint or by searching the users list.
        if (uid == null && token != null) {
            uid = lookupUserIdAfterLogin(token, email);
        }

        TestContext.registeredUserId = uid == null ? null : String.valueOf(uid);

        log.info("First user ready: token={} userId={}",
                token == null ? "NULL" : "***present***",
                TestContext.registeredUserId);
    }

    public static synchronized void ensureSecondUser() {
        if (TestContext.secondUserToken != null && TestContext.secondUserId != null) return;

        String email    = ConfigReader.getSecondUserEmail();
        String password = ConfigReader.getSecondUserPassword();

        try {
            RestAssured.given().relaxedHTTPSValidation()
                    .baseUri(ConfigReader.getBaseUrl())
                    .contentType(ContentType.JSON)
                    .body(PayloadBuilder.registerPayload("API Test User 2", email, password))
                    .when()
                    .post("/api/auth/register")
                    .then().extract().response();
        } catch (Exception ignored) {}

        Response r = RestAssured.given().relaxedHTTPSValidation()
                .baseUri(ConfigReader.getBaseUrl())
                .contentType(ContentType.JSON)
                .body(PayloadBuilder.loginPayload(email, password))
                .when()
                .post("/api/auth/login")
                .then().extract().response();

        if (r.statusCode() < 200 || r.statusCode() >= 300) return;

        TestContext.secondUserToken = firstNonNull(r.path("data.token"), r.path("token"));
        Object uid = firstNonNull(r.path("data.user.userId"), r.path("data.userId"),
                                  r.path("user.userId"), r.path("userId"));
        TestContext.secondUserId = uid == null ? null : String.valueOf(uid);
    }

    /** After login, try common endpoints to retrieve the current user's numeric id. */
    private static Object lookupUserIdAfterLogin(String token, String email) {
        // 1) Common "current user" endpoints.
        String[] meEndpoints = {
                "/api/users/me",
                "/api/auth/me",
                "/api/me",
                "/api/user/me",
                "/api/users/profile",
                "/api/auth/profile"
        };
        for (String ep : meEndpoints) {
            try {
                Response r = RestAssured.given().relaxedHTTPSValidation()
                        .baseUri(ConfigReader.getBaseUrl())
                        .header("Authorization", "Bearer " + token)
                        .when().get(ep)
                        .then().extract().response();
                if (r.statusCode() == 200) {
                    Object id = firstNonNull(
                            r.path("data.userId"), r.path("userId"),
                            r.path("data.id"),     r.path("id"));
                    if (id != null) {
                        log.info("Resolved userId={} via {}", id, ep);
                        return id;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2) Fallback — list all users and match by email.
        try {
            Response r = RestAssured.given().relaxedHTTPSValidation()
                    .baseUri(ConfigReader.getBaseUrl())
                    .header("Authorization", "Bearer " + token)
                    .when().get("/api/users")
                    .then().extract().response();
            if (r.statusCode() == 200) {
                List<Map<String, Object>> users = extractList(r);
                if (users != null) {
                    for (Map<String, Object> u : users) {
                        if (u == null) continue;
                        Object userEmail = u.get("email");
                        if (email.equalsIgnoreCase(String.valueOf(userEmail))) {
                            Object id = u.get("userId") != null ? u.get("userId") : u.get("id");
                            if (id != null) {
                                log.info("Resolved userId={} by searching /api/users", id);
                                return id;
                            }
                        }
                    }
                    log.warn("/api/users returned {} users but none matched email={}", users.size(), email);
                }
            } else {
                log.warn("/api/users status={}", r.statusCode());
            }
        } catch (Exception e) {
            log.warn("/api/users lookup threw: {}", e.getMessage());
        }

        log.warn("Could not resolve userId — tests requiring it will skip.");
        return null;
    }

    // ── Skills ───────────────────────────────────────────────────────────────

    /**
     * Captures one existing skillId from /api/skills and one userSkillId
     * already associated with the primary user (if any). We don't create
     * skills here — the catalog is server-managed.
     */
    public static synchronized void ensureSkill() {
        if (TestContext.skillId != null && TestContext.userSkillId != null) return;
        ensureFirstUser();

        // Catalog — pick the first available skillId.
        if (TestContext.skillId == null) {
            try {
                Response r = RestAssured.given().relaxedHTTPSValidation()
                        .baseUri(ConfigReader.getBaseUrl())
                        .when().get("/api/skills")
                        .then().extract().response();
                if (r.statusCode() == 200) {
                    List<Map<String, Object>> skills = extractList(r);
                    if (skills != null && !skills.isEmpty()) {
                        Object id = skills.get(0).get("skillId");
                        if (id != null) TestContext.skillId = String.valueOf(id);
                    }
                }
            } catch (Exception e) {
                log.warn("/api/skills lookup threw: {}", e.getMessage());
            }
        }

        // User-skills — pick the first one already attached to the seeded user.
        if (TestContext.userSkillId == null
                && TestContext.authToken != null
                && TestContext.registeredUserId != null) {
            try {
                Response r = RestAssured.given().relaxedHTTPSValidation()
                        .baseUri(ConfigReader.getBaseUrl())
                        .header("Authorization", "Bearer " + TestContext.authToken)
                        .when().get("/api/user-skills/" + TestContext.registeredUserId)
                        .then().extract().response();
                if (r.statusCode() == 200) {
                    List<Map<String, Object>> rows = extractList(r);
                    if (rows != null && !rows.isEmpty()) {
                        Object id = rows.get(0).get("userSkillId");
                        if (id != null) TestContext.userSkillId = String.valueOf(id);
                    }
                }
            } catch (Exception e) {
                log.warn("/api/user-skills lookup threw: {}", e.getMessage());
            }
        }
    }

    // ── Sessions ─────────────────────────────────────────────────────────────

    public static void ensureMatch() { /* no-op for now */ }

    /**
     * Captures the first existing sessionId where the primary user is the
     * mentor. We don't create a session here — POST /api/sessions needs a
     * second user + skill, and registration of the second user is flaky
     * across environments. Tests fall back to SkipException if nothing
     * was captured.
     */
    public static synchronized void ensureSession() {
        ensureFirstUser();
        if (TestContext.authToken == null || TestContext.registeredUserId == null) return;
        if (TestContext.sessionId != null) return;

        // Prefer mentor-side, fall back to learner-side.
        String[] paths = {
                "/api/sessions/mentor/"  + TestContext.registeredUserId,
                "/api/sessions/learner/" + TestContext.registeredUserId
        };
        for (String p : paths) {
            try {
                Response r = RestAssured.given().relaxedHTTPSValidation()
                        .baseUri(ConfigReader.getBaseUrl())
                        .header("Authorization", "Bearer " + TestContext.authToken)
                        .when().get(p)
                        .then().extract().response();
                if (r.statusCode() != 200) continue;

                List<Map<String, Object>> rows = extractList(r);
                if (rows == null || rows.isEmpty()) continue;

                // Prefer a Scheduled session — it's safer to mutate (PATCH/DELETE).
                Map<String, Object> chosen = null;
                for (Map<String, Object> row : rows) {
                    if ("Scheduled".equalsIgnoreCase(String.valueOf(row.get("status")))) {
                        chosen = row;
                        break;
                    }
                }
                if (chosen == null) chosen = rows.get(0);

                Object id = chosen.get("sessionId");
                if (id != null) {
                    TestContext.sessionId = String.valueOf(id);
                    return;
                }
            } catch (Exception e) {
                log.warn("{} threw: {}", p, e.getMessage());
            }
        }
    }

    public static void ensureMessage() { /* no-op for now */ }

    // ── Notifications & Reviews ──────────────────────────────────────────────

    /**
     * Captures the first existing notificationId for the primary user, if any.
     * Notifications are usually server-generated, so we don't create one — we just look.
     */
    public static synchronized void ensureNotification() {
        ensureFirstUser();
        if (TestContext.authToken == null || TestContext.registeredUserId == null) return;
        if (TestContext.notificationId != null) return;

        Response r = RestAssured.given()
                .baseUri(ConfigReader.getBaseUrl())
                .header("Authorization", "Bearer " + TestContext.authToken)
                .when()
                .get("/api/notifications/user/" + TestContext.registeredUserId)
                .then().extract().response();

        if (r.statusCode() != 200) return;

        List<Map<String, Object>> data = r.path("data");
        if (data == null || data.isEmpty()) return;

        Object id = data.get(0).get("notificationId");
        if (id != null) TestContext.notificationId = String.valueOf(id);
    }

    /**
     * Captures the first existing reviewId for the primary user (as reviewee), if any.
     * We don't synthesize reviews here — POSTing requires a matching session, which
     * the no-op ensureSession() doesn't provide. Instead we read whatever reviews
     * already exist for the seeded user so later tests have a real id to work with.
     */
    @SuppressWarnings("unchecked")
    public static synchronized void ensureReview() {
        ensureFirstUser();
        if (TestContext.authToken == null || TestContext.registeredUserId == null) return;
        if (TestContext.reviewId != null) return;

        Response r = RestAssured.given().relaxedHTTPSValidation()
                .baseUri(ConfigReader.getBaseUrl())
                .header("Authorization", "Bearer " + TestContext.authToken)
                .when()
                .get("/api/reviews/reviewee/" + TestContext.registeredUserId)
                .then().extract().response();

        if (r.statusCode() != 200) return;

        List<Map<String, Object>> data = r.path("data");
        if (data == null || data.isEmpty()) return;

        Object id = data.get(0).get("reviewId");
        if (id != null) TestContext.reviewId = String.valueOf(id);

        // Also capture the reviewer id + session id from the same record so tests
        // that need a known (session, reviewer) pair can use them.
        Map<String, Object> reviewer = (Map<String, Object>) data.get(0).get("reviewer");
        Map<String, Object> session  = (Map<String, Object>) data.get(0).get("session");
        if (reviewer != null && reviewer.get("userId") != null) {
            TestContext.reviewReviewerId = String.valueOf(reviewer.get("userId"));
        }
        if (session != null && session.get("sessionId") != null) {
            TestContext.reviewSessionId = String.valueOf(session.get("sessionId"));
        }
    }

    // ── Calendar ─────────────────────────────────────────────────────────────

    private static volatile boolean calendarReady = false;

    public static synchronized void ensureCalendarEvent() {
        if (calendarReady) return;
        ensureFirstUser();
        if (TestContext.authToken == null || TestContext.registeredUserId == null) {
            log.warn("Cannot seed calendar event – first user not ready");
            return;
        }

        String future = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        List<Map<String, Object>> payloads = Arrays.asList(
                PayloadBuilder.createCalendarEventPayload(
                        TestContext.registeredUserId, future, "Seeded API test event"),
                PayloadBuilder.createCalendarEventPayloadFlat(
                        TestContext.registeredUserId, future, "Seeded API test event"),
                PayloadBuilder.createCalendarEventPayloadUserRaw(
                        TestContext.registeredUserId, future, "Seeded API test event"),
                PayloadBuilder.createCalendarEventPayloadNoUser(
                        future, "Seeded API test event")
        );

        Response r = null;
        for (Map<String, Object> payload : payloads) {
            r = RestAssured.given()
                    .header("Authorization", "Bearer " + TestContext.authToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                    .post("/api/calendar");
            if (r.statusCode() >= 200 && r.statusCode() < 300) break;
        }

        if (r != null && r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.calendarEventId = r.jsonPath().getString("id");
            log.info("Calendar event seeded via POST: {}", TestContext.calendarEventId);
        } else {
            log.warn("Calendar POST failed → {} body {}. Falling back to existing event lookup.",
                    r == null ? "null" : r.statusCode(),
                    r == null ? "n/a" : r.asString());
            // 1) Look at the registered user's own calendar first.
            String found = findEventForUser(TestContext.authToken, TestContext.registeredUserId);
            // 2) If empty, walk every user via GET /api/users and check each one.
            if (found == null) found = scanAllUsersForAnyEvent(TestContext.authToken);
            if (found != null) {
                TestContext.calendarEventId = found;
                log.info("Reusing existing calendar event id={}", found);
            } else {
                log.warn("No existing calendar events found for any user — downstream tests will skip");
            }
        }
        calendarReady = true;
    }

    private static String findEventForUser(String token, String userId) {
        if (userId == null) return null;
        Response r = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get("/api/calendar/user/" + userId);
        if (r.statusCode() != 200) return null;
        for (String f : new String[]{"id", "_id", "uuid"}) {
            try {
                String v = r.jsonPath().getString("[0]." + f);
                if (v != null && !v.isEmpty()) return v;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static String scanAllUsersForAnyEvent(String token) {
        Response users = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get("/api/users");
        if (users.statusCode() != 200) return null;
        List<Object> ids = null;
        for (String f : new String[]{"id", "_id", "userId", "uuid"}) {
            try {
                List<Object> got = users.jsonPath().getList(f);
                if (got != null && !got.isEmpty()) { ids = got; break; }
            } catch (Exception ignored) { }
        }
        if (ids == null) return null;
        for (Object idObj : ids) {
            if (idObj == null) continue;
            String found = findEventForUser(token, String.valueOf(idObj));
            if (found != null) {
                log.info("Found existing calendar event via user {}", idObj);
                return found;
            }
        }
        return null;
    }

    // ── Other no-op stubs ────────────────────────────────────────────────────

    public static void ensureTransaction() { /* no-op for now */ }
    public static void ensureStory()       { /* no-op for now */ }

    // ── Helpers ──────────────────────────────────────────────────────────────

    @SafeVarargs
    private static <T> T firstNonNull(T... vals) {
        for (T v : vals) if (v != null) return v;
        return null;
    }

    /**
     * Robust list extractor — handles both:
     *   wrapped:  {"success":true,"data":[{...}, ...]}
     *   raw:      [{...}, ...]
     * Returns null on shapes we can't interpret.
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractList(Response r) {
        Object root = r.jsonPath().get();
        if (root instanceof List) {
            return (List<Map<String, Object>>) root;
        }
        if (root instanceof Map) {
            Object data = ((Map<?, ?>) root).get("data");
            if (data instanceof List) return (List<Map<String, Object>>) data;
        }
        return null;
    }

    private Bootstrap() { }
}
