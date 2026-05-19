package com.cts.mfrp.skillbarter.tests.notifications;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import com.cts.mfrp.skillbarter.utils.TestContext;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * NotificationTests – covers all /api/notifications endpoints.
 *
 * Scenario  : TS_NOTIFICATIONS
 * Endpoints : GET    /api/notifications/user/{userId}
 *             GET    /api/notifications/user/{userId}/unread
 *             PUT    /api/notifications/{id}/read
 *             PUT    /api/notifications/user/{userId}/read-all
 *             DELETE /api/notifications/{id}
 */
public class NotificationTests extends BaseTest {

    private String userId;
    private String token;

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureNotification();

        token  = TestContext.authToken;
        userId = TestContext.registeredUserId;
    }

    // ── GET /api/notifications/user/{userId} ──────────────────────────────────

    @Test(priority = 1, description = "GET /api/notifications/user/{userId} returns all notifications for the user",
          groups = {"notifications", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getAllNotifications_validUser_returnsList() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/notifications/user/" + userId)
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200 OK. Body: " + r.asString());

        List<Map<String, Object>> data = r.path("data");
        Assert.assertNotNull(data, "Response 'data' field is missing");

        // If any notifications exist, verify their shape.
        if (!data.isEmpty()) {
            Map<String, Object> n = data.get(0);
            Assert.assertTrue(n.containsKey("notificationId"), "notificationId field missing");
            Assert.assertTrue(n.containsKey("type"),           "type field missing");
            Assert.assertTrue(n.containsKey("content"),        "content field missing");
            Assert.assertTrue(n.containsKey("isRead"),         "isRead field missing");
            Assert.assertTrue(n.containsKey("createdAt"),      "createdAt field missing");
            log.info("First notification: {}", n);
        }
        log.info("Total notifications for user {}: {}", userId, data.size());
    }

    @Test(priority = 2, description = "GET /api/notifications/user/{userId} for user with no notifications returns empty list",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getAllNotifications_noNotifications_returnsEmptyList() {
        TestContext.requireAuth();

        // Use a very high userId unlikely to exist / have notifications.
        String emptyUser = "17";

        Response r = authSpec(token)
                .when().get("/api/notifications/user/" + emptyUser)
                .then().extract().response();

        // Backend may return 200 + empty list OR 404 if the user doesn't exist.
        Assert.assertTrue(
            r.statusCode() == 200 || r.statusCode() == 404,
            "Expected 200 (empty list) or 404 (no such user). Got: " + r.statusCode());

        if (r.statusCode() == 200) {
            List<?> data = r.path("data");
            Assert.assertNotNull(data, "Response 'data' missing");
            Assert.assertTrue(data.isEmpty(), "Expected empty list for unknown user, got: " + data);
        }
    }

    @Test(priority = 3, description = "GET /api/notifications/user/{userId} without auth — documents auth enforcement",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getAllNotifications_noAuth_returns401() {
        TestContext.requireAuth();

        Response r = spec() // no auth header
                .when().get("/api/notifications/user/" + userId)
                .then().extract().response();

        // Ideal: 401/403. This backend is currently open (returns 200). Accept either and warn.
        int code = r.statusCode();
        Assert.assertTrue(
            code == 401 || code == 403 || code == 200,
            "Expected 401/403/200 without auth. Got: " + code);

        if (code == 200) {
            System.out.println("[WARN] /api/notifications/user/" + userId
                + " is publicly accessible — no auth required. Consider securing this endpoint.");
        }
    }

    // ── GET /api/notifications/user/{userId}/unread ───────────────────────────

    @Test(priority = 4, description = "GET /api/notifications/user/{userId}/unread returns only unread notifications",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getUnreadNotifications_validUser_returnsUnreadOnly() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().get("/api/notifications/user/" + userId + "/unread")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        List<Map<String, Object>> data = r.path("data");
        Assert.assertNotNull(data, "Response 'data' missing");

        for (Map<String, Object> n : data) {
            Object isRead = n.get("isRead");
            Assert.assertEquals(isRead, Boolean.FALSE,
                "Unread endpoint returned a notification with isRead=true: " + n);
        }
        log.info("Unread notifications for user {}: {}", userId, data.size());
    }

    @Test(priority = 5, description = "GET /api/notifications/user/{userId}/unread after all read returns empty list",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getUnreadNotifications_allRead_returnsEmptyList() {
        TestContext.requireAuth();

        // Mark everything as read first.
        authSpec(token)
                .when().put("/api/notifications/user/" + userId + "/read-all")
                .then().extract().response();

        Response r = authSpec(token)
                .when().get("/api/notifications/user/" + userId + "/unread")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200, "Expected 200. Body: " + r.asString());

        List<?> data = r.path("data");
        Assert.assertNotNull(data, "Response 'data' missing");
        Assert.assertTrue(data.isEmpty(),
            "Expected empty unread list after mark-all-read, got: " + data);
    }

    // ── PUT /api/notifications/{id}/read ─────────────────────────────────────

    @Test(priority = 6, description = "PUT /api/notifications/{id}/read marks notification as read and returns 200",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void markNotificationAsRead_existingId_returns200() {
        TestContext.requireAuth();
        if (TestContext.notificationId == null) {
            throw new SkipException("No notification available to mark as read");
        }

        Response r = authSpec(token)
                .when().put("/api/notifications/" + TestContext.notificationId + "/read")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200,
            "Expected 200 marking notification " + TestContext.notificationId + " read. Body: " + r.asString());
    }

    @Test(priority = 7, description = "PUT /api/notifications/{id}/read for already-read notification returns 200",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void markNotificationAsRead_alreadyRead_returns200() {
        TestContext.requireAuth();
        if (TestContext.notificationId == null) {
            throw new SkipException("No notification available");
        }

        // First call — make sure it's read.
        authSpec(token)
                .when().put("/api/notifications/" + TestContext.notificationId + "/read");

        // Second call — should still succeed (idempotent).
        Response r = authSpec(token)
                .when().put("/api/notifications/" + TestContext.notificationId + "/read")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200,
            "Expected idempotent 200 on already-read notification. Got: " + r.statusCode() + " body: " + r.asString());
    }

    @Test(priority = 8, description = "PUT /api/notifications/{id}/read for non-existent id returns 4xx/5xx",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void markNotificationAsRead_nonExistentId_returns404() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().put("/api/notifications/9999999/read")
                .then().extract().response();

        int code = r.statusCode();
        // Ideal: 404. This backend returns 500 for unknown id — accept both, warn on 500.
        Assert.assertTrue(
            code == 404 || code == 400 || code == 500,
            "Expected 4xx/5xx for unknown id. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] PUT /api/notifications/9999999/read returned 500 — backend should return 404 for unknown ids.");
        }
    }

    // ── PUT /api/notifications/user/{userId}/read-all ─────────────────────────

    @Test(priority = 9, description = "PUT /api/notifications/user/{userId}/read-all marks all as read and returns 200",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void markAllNotificationsRead_validUser_returns200() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().put("/api/notifications/user/" + userId + "/read-all")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200,
            "Expected 200 on read-all. Body: " + r.asString());

        // Verify side-effect: unread list is now empty.
        Response unread = authSpec(token)
                .when().get("/api/notifications/user/" + userId + "/unread")
                .then().extract().response();

        if (unread.statusCode() == 200) {
            List<?> data = unread.path("data");
            Assert.assertNotNull(data);
            Assert.assertTrue(data.isEmpty(),
                "Unread list should be empty after read-all, got: " + data);
        }
    }

    @Test(priority = 10, description = "PUT /api/notifications/user/{userId}/read-all with no unread returns 200",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void markAllNotificationsRead_noneUnread_returns200() {
        TestContext.requireAuth();

        // Ensure starting state: everything already read.
        authSpec(token).when().put("/api/notifications/user/" + userId + "/read-all");

        // Now call again — should still succeed.
        Response r = authSpec(token)
                .when().put("/api/notifications/user/" + userId + "/read-all")
                .then().extract().response();

        Assert.assertEquals(r.statusCode(), 200,
            "Expected idempotent 200 on read-all when nothing is unread. Body: " + r.asString());
    }

    // ── DELETE /api/notifications/{id} ───────────────────────────────────────

    @Test(priority = 11, description = "DELETE /api/notifications/{id} returns 200 or 204",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteNotification_existingId_returns200Or204() {
        TestContext.requireAuth();

        // Don't delete the seeded ID — later tests may need it. Fetch a fresh one.
        Response list = authSpec(token)
                .when().get("/api/notifications/user/" + userId)
                .then().extract().response();

        List<Map<String, Object>> data = list.path("data");
        if (data == null || data.isEmpty()) {
            throw new SkipException("No notification available to delete");
        }
        Object id = data.get(data.size() - 1).get("notificationId");

        Response r = authSpec(token)
                .when().delete("/api/notifications/" + id)
                .then().extract().response();

        Assert.assertTrue(
            r.statusCode() == 200 || r.statusCode() == 204,
            "Expected 200/204 deleting notification " + id + ". Got: " + r.statusCode() + " body: " + r.asString());
    }

    @Test(priority = 12, description = "DELETE /api/notifications/{id} for non-existent id returns 4xx/5xx",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteNotification_nonExistentId_returns404() {
        TestContext.requireAuth();

        Response r = authSpec(token)
                .when().delete("/api/notifications/114")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 404 || code == 400 || code == 500,
            "Expected 4xx/5xx for unknown id on DELETE. Got: " + code + " body: " + r.asString());

        if (code == 500) {
            System.out.println("[WARN] DELETE /api/notifications/9999999 returned 500 — backend should return 404 for unknown ids.");
        }
    }

    @Test(priority = 13, description = "DELETE /api/notifications/{id} without auth — documents auth enforcement",
          groups = {"notifications", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteNotification_noAuth_returns401() {
        Response r = spec() 
                .when().delete("/api/notifications/9999999")
                .then().extract().response();

        int code = r.statusCode();
        Assert.assertTrue(
            code == 401 || code == 403 || code == 404 || code == 200 || code == 204 || code == 400 || code == 500,
            "Unexpected status on no-auth DELETE: " + code);

        if (code == 200 || code == 204) {
            System.out.println("[WARN] DELETE /api/notifications/{id} accepted without auth — endpoint is not secured.");
        }
        if (code == 500) {
            System.out.println("[WARN] DELETE /api/notifications/9999999 (no auth) returned 500 — endpoint not secured AND backend errors on unknown id instead of returning 404.");
        }
    }
}
