package com.cts.mfrp.skillbarter.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Bootstrap – pre-test data seeding helpers.
 *
 * Each ensure* method is idempotent: it registers / logs in the required
 * user only once and stores the resulting token + ID in TestContext.
 * Call these from @BeforeClass in any test class that needs seeded data.
 *
 * Every request uses .relaxedHTTPSValidation() per-call (corp MITM CAs aren't
 * in the JDK truststore). Progress is logged to stdout so configuration
 * failures are visible in the IntelliJ Run console.
 */
public class Bootstrap {

    static {
        RestAssured.useRelaxedHTTPSValidation();
    }

    private static RequestSpecification req() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .baseUri(ConfigReader.getBaseUrl())
                .accept(ContentType.JSON);
    }

    private static RequestSpecification authReq() {
        return req()
                .header("Authorization", "Bearer " + TestContext.authToken);
    }

    private static void log(String msg) {
        System.out.println("[Bootstrap] " + msg);
    }

    public static void ensureFirstUser() {
        if (TestContext.authToken != null && TestContext.registeredUserId != null) return;

        String email = ConfigReader.getTestEmail();
        String password = ConfigReader.getTestPassword();
        log("ensureFirstUser: email=" + email + " baseUrl=" + ConfigReader.getBaseUrl());

        if (!loginAndStore(email, password, true)) {
            log("ensureFirstUser: first login failed, attempting register");
            register("API Test User", email, password);
            loginAndStore(email, password, true);
        }
        log("ensureFirstUser: token=" + (TestContext.authToken != null ? "SET" : "NULL")
                + " userId=" + TestContext.registeredUserId);
    }

    public static void ensureSecondUser() {
        if (TestContext.secondUserToken != null && TestContext.secondUserId != null) return;

        String email = ConfigReader.getSecondUserEmail();
        String password = ConfigReader.getSecondUserPassword();
        log("ensureSecondUser: email=" + email);

        if (!loginAndStore(email, password, false)) {
            log("ensureSecondUser: first login failed, attempting register");
            register("API Test User Two", email, password);
            loginAndStore(email, password, false);
        }
        log("ensureSecondUser: token=" + (TestContext.secondUserToken != null ? "SET" : "NULL")
                + " userId=" + TestContext.secondUserId);
    }

    public static void ensureSkill() {
        if (TestContext.skillId != null) return;
        ensureFirstUser();

        Response r = authReq().when().get("/api/skills");
        log("ensureSkill: GET /api/skills -> " + r.statusCode());
        if (r.statusCode() == 200) {
            String id = firstId(r);
            if (id != null) TestContext.skillId = id;
        }
        log("ensureSkill: skillId=" + TestContext.skillId);
    }

    public static void ensureMatch() {
        if (TestContext.matchId != null) return;
        ensureFirstUser();
        ensureSecondUser();

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.createMatchPayload(
                        TestContext.registeredUserId, TestContext.secondUserId, 80))
                .when().post("/api/matches");

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.matchId = extractId(r);
        }
    }

    public static void ensureSession() {
        if (TestContext.sessionId != null) return;
        ensureFirstUser();
        ensureSecondUser();
        ensureSkill();
        if (TestContext.skillId == null) {
            log("ensureSession: skipping — skillId is null");
            return;
        }
        if (TestContext.registeredUserId == null || TestContext.secondUserId == null) {
            log("ensureSession: skipping — user ids null");
            return;
        }

        String scheduledAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1).withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.createSessionPayload(
                        TestContext.registeredUserId,
                        TestContext.secondUserId,
                        TestContext.skillId,
                        scheduledAt))
                .when().post("/api/sessions");
        log("ensureSession: POST /api/sessions -> " + r.statusCode());

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.sessionId = extractId(r);
        } else {
            log("ensureSession: body=" + r.asString());
        }
        log("ensureSession: sessionId=" + TestContext.sessionId);
    }

    public static void ensureMessage() {
        if (TestContext.messageId != null) return;
        ensureSession();
        if (TestContext.sessionId == null) return;

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.sendMessagePayload(
                        TestContext.sessionId, TestContext.registeredUserId,
                        "Seed message from Bootstrap"))
                .when().post("/api/messages");

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.messageId = extractId(r);
        }
    }

    public static void ensureNotification() { }

    public static void ensureReview() {
        if (TestContext.reviewId != null) return;
        ensureSession();
        if (TestContext.sessionId == null) return;

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.submitReviewPayload(
                        TestContext.registeredUserId, TestContext.secondUserId,
                        5, "Seed review", TestContext.sessionId))
                .when().post("/api/reviews");

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.reviewId = extractId(r);
        }
    }

    public static void ensureCalendarEvent() {
        if (TestContext.calendarEventId != null) return;
        ensureFirstUser();

        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(2).withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.createCalendarEventPayload(
                        TestContext.registeredUserId, date, "Seed event"))
                .when().post("/api/calendar");

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.calendarEventId = extractId(r);
        }
    }

    public static void ensureTransaction() {
        if (TestContext.transactionId != null) return;
        ensureSession();
        if (TestContext.sessionId == null) {
            log("ensureTransaction: skipping — sessionId is null");
            return;
        }

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.createTransactionPayload(
                        TestContext.registeredUserId, TestContext.sessionId,
                        100.0, "UPI"))
                .when().post("/api/transactions");
        log("ensureTransaction: POST /api/transactions -> " + r.statusCode());

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.transactionId = extractId(r);
        } else {
            log("ensureTransaction: body=" + r.asString());
        }
        log("ensureTransaction: transactionId=" + TestContext.transactionId);
    }

    public static void ensureStory() {
        if (TestContext.storyId != null) return;
        ensureFirstUser();

        Response r = authReq().contentType(ContentType.JSON)
                .body(PayloadBuilder.createStoryPayload(
                        TestContext.registeredUserId, "Seed Story",
                        "Seed story content", null))
                .when().post("/api/stories");

        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            TestContext.storyId = extractId(r);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void register(String name, String email, String password) {
        Response r = req().contentType(ContentType.JSON)
                .body(PayloadBuilder.registerPayload(name, email, password))
                .when().post("/api/auth/register");
        log("register: POST /api/auth/register -> " + r.statusCode());
    }

    private static boolean loginAndStore(String email, String password, boolean primary) {
        Response r;
        try {
            r = req().contentType(ContentType.JSON)
                    .body(PayloadBuilder.loginPayload(email, password))
                    .when().post("/api/auth/login");
        } catch (Exception e) {
            log("loginAndStore: REQUEST FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
        log("loginAndStore: POST /api/auth/login (" + email + ") -> " + r.statusCode());

        if (r.statusCode() != 200) {
            log("loginAndStore: login non-200 body=" + r.asString());
            return false;
        }

        String token = r.jsonPath().getString("token");
        if (token == null) token = r.jsonPath().getString("accessToken");
        if (token == null) {
            log("loginAndStore: token missing in response body=" + r.asString());
            return false;
        }

        String userId = lookupUserIdByEmail(email, token);

        if (primary) {
            TestContext.authToken = token;
            TestContext.registeredUserId = userId;
        } else {
            TestContext.secondUserToken = token;
            TestContext.secondUserId = userId;
        }
        return userId != null;
    }

    @SuppressWarnings("unchecked")
    private static String lookupUserIdByEmail(String email, String token) {
        Response r = RestAssured.given()
                .relaxedHTTPSValidation()
                .baseUri(ConfigReader.getBaseUrl())
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when().get("/api/users");
        log("lookupUserIdByEmail: GET /api/users -> " + r.statusCode());
        if (r.statusCode() != 200) return null;
        java.util.List<java.util.Map<String, Object>> users;
        try {
            users = r.as(java.util.List.class);
        } catch (Exception e) {
            log("lookupUserIdByEmail: failed to parse list: " + e.getMessage());
            return null;
        }
        if (users == null) return null;
        for (java.util.Map<String, Object> u : users) {
            Object e = u.get("email");
            if (e != null && email.equalsIgnoreCase(e.toString())) {
                Object id = u.get("userId");
                if (id == null) id = u.get("id");
                return id == null ? null : id.toString();
            }
        }
        log("lookupUserIdByEmail: no user matched email " + email + " in list of size " + users.size());
        return null;
    }

    private static String extractId(Response r) {
        String id = r.jsonPath().getString("transactionId");
        if (id == null) id = r.jsonPath().getString("sessionId");
        if (id == null) id = r.jsonPath().getString("messageId");
        if (id == null) id = r.jsonPath().getString("reviewId");
        if (id == null) id = r.jsonPath().getString("notificationId");
        if (id == null) id = r.jsonPath().getString("calendarEventId");
        if (id == null) id = r.jsonPath().getString("storyId");
        if (id == null) id = r.jsonPath().getString("matchId");
        if (id == null) id = r.jsonPath().getString("userId");
        if (id == null) id = r.jsonPath().getString("skillId");
        if (id == null) id = r.jsonPath().getString("id");
        if (id == null) id = r.jsonPath().getString("_id");
        if (id == null) id = r.jsonPath().getString("data.id");
        return id;
    }

    private static String firstId(Response r) {
        try {
            String id = r.jsonPath().getString("[0].skillId");
            if (id == null) id = r.jsonPath().getString("[0].id");
            if (id == null) id = r.jsonPath().getString("[0]._id");
            if (id == null) id = r.jsonPath().getString("content[0].skillId");
            if (id == null) id = r.jsonPath().getString("content[0].id");
            return id;
        } catch (Exception e) {
            return null;
        }
    }

    private Bootstrap() { }
}
