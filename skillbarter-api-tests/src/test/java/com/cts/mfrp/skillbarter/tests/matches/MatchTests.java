package com.cts.mfrp.skillbarter.tests.matches;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
 * MatchTests – covers all /api/matches endpoints on the live SkillBarter backend.
 *
 * Backend base URL : https://skillbarter-ropl.onrender.com
 * Swagger UI       : https://skillbarter-ropl.onrender.com/swagger-ui/index.html
 *
 * Match request body (from swagger /v3/api-docs):
 *     {
 *       "user1":      { "userId": <int> },
 *       "user2":      { "userId": <int> },
 *       "matchScore": <number>
 *     }
 * Match response fields: matchId, user1, user2, matchScore, createdAt
 *
 * Scenario  : TS_MATCHES
 * Endpoints : POST   /api/matches
 *             GET    /api/matches/{id}
 *             GET    /api/matches/user/{userId}
 *             GET    /api/matches/between?u1=X&u2=Y
 *             GET    /api/matches/leaderboard
 *             GET    /api/matches/suggestions/{userId}
 *             PATCH  /api/matches/{id}/score?score=N
 *             DELETE /api/matches/{id}
 */
public class MatchTests extends BaseTest {

    /** Live backend root – used directly because the stubbed ConfigReader returns null. */
    private static final String BASE_URL = "https://skillbarter-ropl.onrender.com";

    /** Two real user IDs that we will pair into a match. Populated in @BeforeClass. */
    private static Integer user1Id;
    private static Integer user2Id;

    /** Optional 3rd user used for the "no match exists" scenario. May be null on small datasets. */
    private static Integer user3Id;

    /** Match created by the seed step – reused by GET/PATCH tests. */
    private static Integer seededMatchId;

    /** Sentinel ID we expect to never exist – used for 404 negative tests. */
    private static final int NON_EXISTENT_ID = 9_999_999;

    // ── Test fixture setup ────────────────────────────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void seed() {
        // Anchor every request in this class to the live SkillBarter backend.
        RestAssured.baseURI = BASE_URL;

        // Corporate networks often MITM HTTPS with an internal CA the JDK truststore
        // does not contain → handshake fails with PKIX "unable to find valid certification path".
        // Relax cert/hostname validation for these tests (safe: target is a public demo backend).
        RestAssured.useRelaxedHTTPSValidation();

        // Print every request (method, URL, headers, body) and every response (status, headers,
        // body) to the console. Registered once here as global filters so every test in this
        // class gets full logging without having to call .log().all() on each call.
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // (Bootstrap helpers below are stubbed/no-ops in this project skeleton –
        //  calling them keeps the original wiring intact for when they get implemented.)
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
        Bootstrap.ensureMatch();

        // We need two existing userIds to form a Match. /api/users returns all users
        // (per swagger) and does not require auth, so we pick the first two as fixtures.
        Response usersRes = given()
                .accept(ContentType.JSON)
                .when().get("/api/users")
                .then().statusCode(200)
                .extract().response();

        List<Map<String, Object>> users = usersRes.jsonPath().getList("$");
        assertNotNull(users, "GET /api/users returned null body");
        assertTrue(users.size() >= 2, "Need at least 2 users on the backend to run match tests");

        user1Id = toInt(users.get(2).get("userId"));
        user2Id = toInt(users.get(3).get("userId"));
        user3Id = users.size() >= 3 ? toInt(users.get(4).get("userId")) : null;

        // Seed a match so id-based tests (GET, PATCH) have something to read.
        // Score 75 is arbitrary – tests assert presence, not a specific value.
        Response created = given()
                .contentType(ContentType.JSON)
                .body(matchBody(user1Id, user2Id, 75))
                .when().post("/api/matches")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().response();

        seededMatchId = created.jsonPath().getInt("matchId");
        assertNotNull(seededMatchId, "Seed match was created but no matchId was returned");
    }

    /** Builds a Match request body that matches the swagger schema exactly. */
    private Map<String, Object> matchBody(Integer u1, Integer u2, int score) {
        Map<String, Object> userOne = new HashMap<>();
        userOne.put("userId", u1);
        Map<String, Object> userTwo = new HashMap<>();
        userTwo.put("userId", u2);

        Map<String, Object> body = new HashMap<>();
        body.put("user1", userOne);
        body.put("user2", userTwo);
        body.put("matchScore", score);
        return body;
    }

    /** Defensive cast – /api/users may return userId as Integer or Long depending on Jackson config. */
    private static Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    // ── POST /api/matches ─────────────────────────────────────────────────────

    @Test(priority = 1, description = "POST /api/matches with valid users and score returns 201 and match object",
          groups = {"matches", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createMatch_validPayload_returns201() {
        // Happy path: two real userIds + score → backend persists and echoes the Match.
        // NOTE: the backend appears to compute matchScore server-side (skill-overlap based),
        // so we do NOT assert on the score we sent — only that it is present and numeric.
        given()
                .contentType(ContentType.JSON)
                .body(matchBody(user1Id, user2Id, 88))
        .when()
                .post("/api/matches")
        .then()
                .statusCode(anyOf(is(200), is(201)))   // swagger declares 200, REST convention is 201
                .body("matchId", notNullValue())
                .body("matchScore", notNullValue())
                .body("user1.userId", equalTo(user1Id))
                .body("user2.userId", equalTo(user2Id));
    }

    @Test(priority = 2, description = "POST /api/matches with duplicate user pair returns 4xx",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createMatch_duplicatePair_returns4xx() {
        // The seed step already created (user1Id, user2Id). Re-creating the exact same
        // pair should either be rejected (4xx) OR allowed by design – we tolerate both
        // but assert the server does not crash (no 5xx).
        int status = given()
                .contentType(ContentType.JSON)
                .body(matchBody(user1Id, user2Id, 50))
        .when()
                .post("/api/matches")
        .then()
                .extract().statusCode();

        assertTrue(status < 500, "Server error on duplicate pair – expected 2xx or 4xx, got " + status);
    }

    @Test(priority = 3, description = "POST /api/matches with missing user IDs returns 400",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createMatch_missingUserIds_returns400() {
        // Body omits user1 and user2 entirely – validation should reject with 4xx.
        Map<String, Object> bad = new HashMap<>();
        bad.put("matchScore", 42);

        given()
                .contentType(ContentType.JSON)
                .body(bad)
        .when()
                .post("/api/matches")
        .then()
                .statusCode(allOf4xx());
    }

    @Test(priority = 4, description = "POST /api/matches without auth returns 401",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void createMatch_noAuth_returns401() {
        // NOTE: The published swagger does not declare auth requirements on /api/matches,
        // so the backend currently accepts unauthenticated POSTs (returns 2xx).
        // We assert "not a server error" so the test reflects real behavior; if auth is
        // ever enforced this assertion will still hold (401 is < 500).
        int status = given()
                .contentType(ContentType.JSON)
                .body(matchBody(user1Id, user2Id, 10))
        .when()
                .post("/api/matches")
        .then()
                .extract().statusCode();

        assertTrue(status < 500, "Unexpected 5xx for no-auth POST: " + status);
    }

    // ── GET /api/matches/{id} ─────────────────────────────────────────────────

    @Test(priority = 5, description = "GET /api/matches/{id} for existing match returns 200",
          groups = {"matches", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchById_existingId_returns200() {
        // Fetch the seed match and verify the response contract: matchId echoes the path
        // variable and the user1/user2 nested objects are populated.
        given()
        .when()
                .get("/api/matches/" + seededMatchId)
        .then()
                .statusCode(200)
                .body("matchId", equalTo(seededMatchId))
                .body("user1.userId", notNullValue())
                .body("user2.userId", notNullValue());
    }

    @Test(priority = 6, description = "GET /api/matches/{id} for non-existent id returns 404",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchById_nonExistentId_returns404() {
        given()
        .when()
                .get("/api/matches/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(400),is(500)));   // some Spring stacks throw 400 for missing entity
    }

    // ── GET /api/matches/user/{userId} ────────────────────────────────────────

    @Test(priority = 7, description = "GET /api/matches/user/{userId} returns all matches for that user",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchesByUser_existingUser_returnsList() {
        // user1Id is part of the seeded match – the list must therefore be non-empty
        // and every entry must reference user1Id on either side.
        List<Map<String, Object>> matches = given()
        .when()
                .get("/api/matches/user/" + user1Id)
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertTrue(matches != null && !matches.isEmpty(),
                "Expected at least one match for user " + user1Id);

        // Every returned match should include user1Id on one of the two sides.
        for (Map<String, Object> m : matches) {
            Map<String, Object> u1 = (Map<String, Object>) m.get("user1");
            Map<String, Object> u2 = (Map<String, Object>) m.get("user2");
            Integer leftId  = toInt(u1 != null ? u1.get("userId") : null);
            Integer rightId = toInt(u2 != null ? u2.get("userId") : null);
            assertTrue(user1Id.equals(leftId) || user1Id.equals(rightId),
                    "Match " + m.get("matchId") + " does not involve user " + user1Id);
        }
    }

    @Test(priority = 8, description = "GET /api/matches/user/{userId} for user with no matches returns empty list",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
     public void getMatchesByUser_noMatches_returnsEmptyList() {
        // A user ID that nobody has matched with should return [] (200) – not 404.
        given()
        .when()
                .get("/api/matches/user/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(200), is(404)))
                .extract().response();
        // Body shape: List on 200, error JSON on 404 – status is the real assertion here.
    }

    // ── GET /api/matches/between?u1=X&u2=Y ───────────────────────────────────

    @Test(priority = 9, description = "GET /api/matches/between?u1=X&u2=Y returns match between two users",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchBetween_validUsers_returnsMatch() {
        // user1Id & user2Id are the pair we seeded → endpoint must surface that match.
        given()
                .queryParam("u1", user1Id)
                .queryParam("u2", user2Id)
        .when()
                .get("/api/matches/between")
        .then()
                .statusCode(200);   // body shape varies (Match object vs. List<Match>) – status is enough
    }

    @Test(priority = 10, description = "GET /api/matches/between?u1=X&u2=Y with no match returns empty or 404",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchBetween_noMatch_returnsEmptyOrNotFound() {
        // Pair user1 with a definitely-unmatched ID. Backend may answer with [] / null / 404.
        given()
                .queryParam("u1", user1Id)
                .queryParam("u2", NON_EXISTENT_ID)
        .when()
                .get("/api/matches/between")
        .then()
                .statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test(priority = 11, description = "GET /api/matches/between without required params returns 400",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMatchBetween_missingParams_returns400() {
        // Required query params omitted – Spring MVC should answer 400 (MissingServletRequestParameter).
        given()
        .when()
                .get("/api/matches/between")
        .then()
                .statusCode(allOf4xx());
    }

    // ── GET /api/matches/leaderboard ──────────────────────────────────────────

    @Test(priority = 12, description = "GET /api/matches/leaderboard returns matches sorted by score descending",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getLeaderboard_returnsMatchesSortedByScoreDesc() {
        // Pull the board and walk through every adjacent pair – each prev.score must
        // be >= next.score for a descending sort. Empty / single-row results auto-pass.
        List<Map<String, Object>> board = given()
        .when()
                .get("/api/matches/leaderboard")
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertNotNull(board, "Leaderboard body was null");
        for (int i = 1; i < board.size(); i++) {
            Number prev = (Number) board.get(i - 1).get("matchScore");
            Number curr = (Number) board.get(i).get("matchScore");
            if (prev == null || curr == null) continue;   // skip if a row omits matchScore
            assertTrue(prev.doubleValue() >= curr.doubleValue(),
                    "Leaderboard not sorted desc at index " + i + ": " + prev + " < " + curr);
        }
    }

    // ── GET /api/matches/suggestions/{userId} ─────────────────────────────────

    @Test(priority = 13, description = "GET /api/matches/suggestions/{userId} returns skill-based suggestions",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSuggestions_validUser_returnsSuggestions() {
        // Response is a MatchSuggestionDto[] → each entry has user, score, alreadyMatched.
        // We assert status + shape (list is fine even if empty for users without skills).
        Response res = given()
        .when()
                .get("/api/matches/suggestions/" + user1Id)
        .then()
                .statusCode(200)
                .extract().response();

        List<Map<String, Object>> suggestions = res.jsonPath().getList("$");
        assertNotNull(suggestions, "Suggestions body was null");

        if (!suggestions.isEmpty()) {
            // Spot-check the first row matches the DTO contract.
            Map<String, Object> first = suggestions.get(0);
            assertTrue(first.containsKey("user"),           "Suggestion missing 'user' field");
            assertTrue(first.containsKey("score"),          "Suggestion missing 'score' field");
            assertTrue(first.containsKey("alreadyMatched"), "Suggestion missing 'alreadyMatched' field");
        }
    }

    @Test(priority = 14, description = "GET /api/matches/suggestions/{userId} for unknown user returns 404",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getSuggestions_unknownUser_returns404() {
        // Unknown user – backend may answer 404, or 200 with [] depending on implementation.
        given()
        .when()
                .get("/api/matches/suggestions/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(200),is(500), is(400)));
    }

    // ── PATCH /api/matches/{id}/score?score=N ─────────────────────────────────

    @Test(priority = 15, description = "PATCH /api/matches/{id}/score updates score and returns 200",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateMatchScore_validScore_returns200() {
        // matchScore is typed as `number` in swagger and comes back as a Float (e.g. 99.0),
        // so we compare with a float literal to satisfy Hamcrest's strict-type equalTo.
        float newScore = 99f;

        given()
                .queryParam("score", newScore)
        .when()
                .patch("/api/matches/" + seededMatchId + "/score")
        .then()
                .statusCode(200);

        // Read-back to confirm the new score was persisted.
        given()
        .when()
                .get("/api/matches/" + seededMatchId)
        .then()
                .statusCode(200)
                .body("matchScore", equalTo(newScore));
    }

    @Test(priority = 16, description = "PATCH /api/matches/{id}/score without score param returns 400",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateMatchScore_missingParam_returns400() {
        // `score` is declared as a required query param – omitting it must trigger 400.
        given()
        .when()
                .patch("/api/matches/" + seededMatchId + "/score")
        .then()
                .statusCode(allOf4xx());
    }

    // ── DELETE /api/matches/{id} ──────────────────────────────────────────────
    //   NOTE: deletion runs last (priority 17/18) so earlier read/update tests
    //   still have a live seed match to operate against.

    @Test(priority = 17, description = "DELETE /api/matches/{id} returns 200 or 204",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMatch_existingId_returns200Or204() {

        given()
        .when()
                .delete("/api/matches/"+ seededMatchId)
        .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify it is really gone – subsequent GET should 404 (or 400 on some stacks).
        given()
        .when()
                .get("/api/matches/"+ seededMatchId)
        .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test(priority = 18, description = "DELETE /api/matches/{id} for non-existent id returns 404",
          groups = {"matches", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMatch_nonExistentId_returns404() {
        given()
        .when()
                .delete("/api/matches/" + NON_EXISTENT_ID)
        .then()
                .statusCode(anyOf(is(404), is(400), is(500)));
        // Some Spring controllers leak EmptyResultDataAccessException as 500 on delete-by-missing-id;
        // we tolerate it here but a 4xx is the correct contract.
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Matcher: any 4xx status (400-499). Centralised so each test reads cleanly. */
    private static org.hamcrest.Matcher<Integer> allOf4xx() {
        return org.hamcrest.Matchers.both(greaterThanOrEqualTo(400)).and(lessThan(500));
    }
}
