package com.cts.mfrp.skillbarter.tests.stories;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.PayloadBuilder;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import com.cts.mfrp.skillbarter.utils.TestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

/**
 * StoryTests – /api/stories endpoints, written in REST Assured BDD style
 * (.given().when().then() with Hamcrest matchers).
 *
 * Live backend: https://skillbarter-ropl.onrender.com (verified 2026-05-19).
 * Schema: CommunityStory { storyId:int, user:User, title, content, mediaUrl?,
 *   createdAt, updatedAt }. Required on create: title, content, user.
 *
 * 7 tests fail against the current backend — each surfaces a real defect:
 *  - no auth enforcement on /api/stories (4: noAuth POST, 7: noAuth GET)
 *  - NoSuchElementException leaks as 500 for unknown id on GET/PUT/DELETE
 *    (9, 17, 20)
 *  - no ownership check on PUT/DELETE — any user can mutate any story
 *    (16, 19)
 */
public class StoryTests extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
        Bootstrap.ensureStory();

        if (TestContext.authToken == null || TestContext.registeredUserId == null
                || TestContext.storyId == null) {
            throw new IllegalStateException("Bootstrap failed to seed StoryTests — "
                    + "check [Bootstrap] log lines above.");
        }
    }

    // ── POST /api/stories ─────────────────────────────────────────────────────

    @Test(priority = 1, groups = {"stories", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/stories with valid user, title, content returns 201 and story object")
    public void createStory_validPayload_returns201() {
        given().spec(authSpec(TestContext.authToken))
                .body(storyPayload("API Story " + System.currentTimeMillis(), "Body content", null))
        .when()
                .post("/api/stories")
        .then()
                .statusCode(201)
                .body("storyId", notNullValue())
                .body("title", containsString("API Story"))
                .body("user.userId", equalTo(Integer.parseInt(TestContext.registeredUserId)));
    }

    @Test(priority = 2, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/stories with missing title returns 400")
    public void createStory_missingTitle_returns400() {
        Map<String, Object> body = storyPayload(null, "content only", null);
        body.remove("title");

        given().spec(authSpec(TestContext.authToken)).body(body)
        .when()
                .post("/api/stories")
        .then()
                .statusCode(400);
    }

    @Test(priority = 3, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/stories with missing content returns 400")
    public void createStory_missingContent_returns400() {
        Map<String, Object> body = storyPayload("title only", null, null);
        body.remove("content");

        given().spec(authSpec(TestContext.authToken)).body(body)
        .when()
                .post("/api/stories")
        .then()
                .statusCode(400);
    }

    @Test(priority = 4, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/stories without auth returns 401")
    public void createStory_noAuth_returns401() {
        given().spec(spec())
                .body(storyPayload("No-auth story", "Body", null))
        .when()
                .post("/api/stories")
        .then()
                .statusCode(401);
    }

    @Test(priority = 5, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/stories with optional mediaUrl returns 201")
    public void createStory_withMediaUrl_returns201() {
        String media = "https://example.com/img.png";
        given().spec(authSpec(TestContext.authToken))
                .body(storyPayload("Story with media " + System.currentTimeMillis(), "Body", media))
        .when()
                .post("/api/stories")
        .then()
                .statusCode(201)
                .body("mediaUrl", equalTo(media));
    }

    // ── GET /api/stories ──────────────────────────────────────────────────────

    @Test(priority = 6, groups = {"stories", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories returns all stories ordered by newest first")
    public void getAllStories_returnsListNewestFirst() {
        java.util.List<String> createdAts = given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories")
        .then()
                .statusCode(200)
                .extract().jsonPath().getList("createdAt", String.class);

        for (int i = 1; i < createdAts.size(); i++) {
            Assert.assertTrue(createdAts.get(i - 1).compareTo(createdAts.get(i)) >= 0,
                    "Stories should be newest-first: " + createdAts.get(i - 1)
                            + " came before " + createdAts.get(i));
        }
    }

    @Test(priority = 7, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories without auth returns 401")
    public void getAllStories_noAuth_returns401() {
        given().spec(spec())
        .when()
                .get("/api/stories")
        .then()
                .statusCode(401);
    }

    // ── GET /api/stories/{id} ─────────────────────────────────────────────────

    @Test(priority = 8, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/{id} for existing story returns 200 and story details")
    public void getStoryById_existingId_returns200() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories/" + TestContext.storyId)
        .then()
                .statusCode(200)
                .body("storyId", equalTo(Integer.parseInt(TestContext.storyId)))
                .body("user.userId", notNullValue())
                .body("title", notNullValue())
                .body("content", notNullValue());
    }

    @Test(priority = 9, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/{id} for non-existent id returns 404")
    public void getStoryById_nonExistentId_returns404() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories/99999999")
        .then()
                .statusCode(404);
    }

    // ── GET /api/stories/user/{userId} ────────────────────────────────────────

    @Test(priority = 10, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/user/{userId} returns all stories by that user")
    public void getStoriesByUser_validUser_returnsList() {
        int uid = Integer.parseInt(TestContext.registeredUserId);
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories/user/" + uid)
        .then()
                .statusCode(200)
                .body("user.userId", everyItem(equalTo(uid)));
    }

    @Test(priority = 11, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/user/{userId} for user with no stories returns empty list")
    public void getStoriesByUser_noStories_returnsEmptyList() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories/user/99999999")
        .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    // ── GET /api/stories/search?keyword=X ─────────────────────────────────────

    @Test(priority = 12, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/search?keyword=X returns stories matching title keyword")
    public void searchStories_validKeyword_returnsMatches() {
        // Seed a story with a unique keyword so the assertion is deterministic
        String keyword = "uniq" + System.currentTimeMillis();
        given().spec(authSpec(TestContext.authToken))
                .body(storyPayload("Search target " + keyword, "Body", null))
        .when()
                .post("/api/stories")
        .then()
                .statusCode(201);

        given().spec(authSpec(TestContext.authToken)).queryParam("keyword", keyword)
        .when()
                .get("/api/stories/search")
        .then()
                .statusCode(200)
                .body("title", everyItem(containsString(keyword)));
    }

    @Test(priority = 13, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/search?keyword=X with no match returns empty list")
    public void searchStories_noMatch_returnsEmptyList() {
        given().spec(authSpec(TestContext.authToken))
                .queryParam("keyword", "zzz_no_match_" + System.currentTimeMillis())
        .when()
                .get("/api/stories/search")
        .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test(priority = 14, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/stories/search without keyword param returns 400")
    public void searchStories_missingKeyword_returns400() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/stories/search")
        .then()
                .statusCode(400);
    }

    // ── PUT /api/stories/{id} ─────────────────────────────────────────────────

    @Test(priority = 15, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "PUT /api/stories/{id} with updated title and content returns 200")
    public void updateStory_validPayload_returns200() {
        String id = createOwnStory("PUT-target " + System.currentTimeMillis(), "original");

        given().spec(authSpec(TestContext.authToken))
                .body(storyPayload("Updated title", "Updated content", null))
        .when()
                .put("/api/stories/" + id)
        .then()
                .statusCode(200)
                .body("title", equalTo("Updated title"))
                .body("content", equalTo("Updated content"))
                .body("updatedAt", notNullValue());
    }

    @Test(priority = 16, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "PUT /api/stories/{id} by different user returns 403")
    public void updateStory_differentUser_returns403() {
        TestContext.requireSecondUser();
        String id = createOwnStory("Ownership-target " + System.currentTimeMillis(), "owned by first user");

        Map<String, Object> body = PayloadBuilder.createStoryPayload(
                TestContext.secondUserId, "Hijack attempt", "Hijack content", null);

        given().spec(authSpec(TestContext.secondUserToken)).body(body)
        .when()
                .put("/api/stories/" + id)
        .then()
                .statusCode(403);
    }

    @Test(priority = 17, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "PUT /api/stories/{id} for non-existent id returns 404")
    public void updateStory_nonExistentId_returns404() {
        given().spec(authSpec(TestContext.authToken))
                .body(storyPayload("Nope", "Nope", null))
        .when()
                .put("/api/stories/99999999")
        .then()
                .statusCode(404);
    }

    // ── DELETE /api/stories/{id} ──────────────────────────────────────────────

    @Test(priority = 18, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "DELETE /api/stories/{id} by author returns 200 or 204")
    public void deleteStory_byAuthor_returns200Or204() {
        String id = createOwnStory("DELETE-target " + System.currentTimeMillis(), "will be deleted");

        given().spec(authSpec(TestContext.authToken))
        .when()
                .delete("/api/stories/" + id)
        .then()
                .statusCode(is(oneOf(200, 204)));
    }

    @Test(priority = 19, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "DELETE /api/stories/{id} by different user returns 403")
    public void deleteStory_differentUser_returns403() {
        TestContext.requireSecondUser();
        String id = createOwnStory("Delete-hijack target " + System.currentTimeMillis(), "owned by first user");

        given().spec(authSpec(TestContext.secondUserToken))
        .when()
                .delete("/api/stories/" + id)
        .then()
                .statusCode(403);
    }

    @Test(priority = 20, groups = {"stories", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "DELETE /api/stories/{id} for non-existent id returns 404")
    public void deleteStory_nonExistentId_returns404() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .delete("/api/stories/99999999")
        .then()
                .statusCode(404);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static Map<String, Object> storyPayload(String title, String content, String mediaUrl) {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> userRef = new HashMap<>();
        userRef.put("userId", Long.parseLong(TestContext.registeredUserId));
        body.put("user", userRef);
        if (title != null) body.put("title", title);
        if (content != null) body.put("content", content);
        body.put("mediaUrl", mediaUrl);
        return body;
    }

    private String createOwnStory(String title, String content) {
        return given().spec(authSpec(TestContext.authToken))
                .body(storyPayload(title, content, null))
        .when()
                .post("/api/stories")
        .then()
                .statusCode(201)
                .extract().jsonPath().getString("storyId");
    }
}
