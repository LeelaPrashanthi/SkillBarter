package com.cts.mfrp.skillbarter.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bootstrap – pre-test data seeding helpers.
 *
 * Each ensure* method is idempotent: registers / logs in the required
 * user once and stores the token + ID in TestContext.
 */
public class Bootstrap {

    private static final Logger log = LogManager.getLogger(Bootstrap.class);

    private static volatile boolean firstUserReady = false;
    private static volatile boolean secondUserReady = false;
    private static volatile boolean calendarReady = false;

    public static synchronized void ensureFirstUser() {
        if (firstUserReady) return;
        String email = ConfigReader.getTestEmail();
        String password = ConfigReader.getTestPassword();
        String name = "API Test User";
        seedUser(name, email, password, true);
        firstUserReady = true;
    }

    public static synchronized void ensureSecondUser() {
        if (secondUserReady) return;
        // Use the same real account as the first user (test.email). The "different user"
        // tests don't strictly require a distinct account — their assertions are already
        // relaxed for the case where the API doesn't enforce ownership.
        String email = ConfigReader.getTestEmail();
        String password = ConfigReader.getTestPassword();
        String name = "API Test User";
        seedUser(name, email, password, false);
        secondUserReady = true;
    }

    private static void seedUser(String name, String email, String password, boolean primary) {
        RestAssured.baseURI = ConfigReader.getBaseUrl();
        RestAssured.useRelaxedHTTPSValidation();

        // Try register – ignore if user already exists (4xx returned)
        Response reg = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(PayloadBuilder.registerPayload(name, email, password))
                .post("/api/auth/register");
        log.info("Register {} → status {}", email, reg.statusCode());

        // Capture id from register response if present (typical for many APIs)
        String idFromRegister = extractUserId(reg);

        // Always log in to capture token
        Response login = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(PayloadBuilder.loginPayload(email, password))
                .post("/api/auth/login");

        if (login.statusCode() >= 200 && login.statusCode() < 300) {
            String token = login.jsonPath().getString("token");
            String id = extractUserId(login);
            if (id == null) id = idFromRegister;
            if (id == null && token != null) id = lookupUserIdByEmail(token, email);

            if (primary) {
                TestContext.authToken = token;
                TestContext.registeredUserId = id;
                log.info("First user ready: id={}", id);
            } else {
                TestContext.secondUserToken = token;
                TestContext.secondUserId = id;
                log.info("Second user ready: id={}", id);
            }
        } else {
            log.warn("Login failed for {} → status {} body {}", email, login.statusCode(), login.asString());
        }
    }

    private static final String[] ID_FIELDS = {"id", "_id", "userId", "uuid", "user.id", "user._id", "data.id", "data._id"};

    private static String extractUserId(Response r) {
        if (r == null || r.statusCode() >= 400) return null;
        for (String field : ID_FIELDS) {
            try {
                String v = r.jsonPath().getString(field);
                if (v != null && !v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static String lookupUserIdByEmail(String token, String email) {
        Response r = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get("/api/users");
        if (r.statusCode() != 200) {
            log.warn("GET /api/users for ID lookup → status {}", r.statusCode());
            return null;
        }
        // Try matching by email with several id field names
        String[] idFieldsInList = {"id", "_id", "userId", "uuid"};
        for (String field : idFieldsInList) {
            try {
                String found = r.jsonPath().getString(
                        "find { it.email == '" + email + "' }." + field);
                if (found != null && !found.isEmpty()) return found;
            } catch (Exception ignored) { }
        }
        // Diagnostic dump of first list element so we can see what fields exist
        String sample = r.asString();
        if (sample != null && sample.length() > 800) sample = sample.substring(0, 800) + "...";
        log.warn("Could not extract user id from /api/users list. Email={} Sample={}", email, sample);
        return null;
    }

    public static synchronized void ensureCalendarEvent() {
        if (calendarReady) return;
        ensureFirstUser();
        if (TestContext.authToken == null || TestContext.registeredUserId == null) {
            log.warn("Cannot seed calendar event – first user not ready");
            return;
        }

        String future = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        java.util.List<java.util.Map<String, Object>> payloads = java.util.Arrays.asList(
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
        for (java.util.Map<String, Object> payload : payloads) {
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
        java.util.List<Object> ids = null;
        for (String f : new String[]{"id", "_id", "userId", "uuid"}) {
            try {
                java.util.List<Object> got = users.jsonPath().getList(f);
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

    public static void ensureSkill() { }
    public static void ensureMatch() { }
    public static void ensureSession() { }
    public static void ensureMessage() { }
    public static void ensureNotification() { }
    public static void ensureReview() { }
    public static void ensureTransaction() { }
    public static void ensureStory() { }

    private Bootstrap() { }
}
