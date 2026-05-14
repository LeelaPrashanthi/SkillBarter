package com.cts.mfrp.skillbarter.tests.messages;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * MessageTests – covers all /api/messages endpoints.
 *
 * Scenario  : TS_MESSAGES
 * Endpoints : POST   /api/messages
 *             GET    /api/messages/session/{sessionId}
 *             GET    /api/messages/sender/{senderId}
 *             GET    /api/messages/{messageId}
 *             DELETE /api/messages/{messageId}
 */
public class MessageTests extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
        Bootstrap.ensureSession();
        Bootstrap.ensureMessage();
    }

    // ── POST /api/messages ────────────────────────────────────────────────────

    @Test(priority = 1, description = "POST /api/messages with valid sessionId, senderId, content returns 201",
          groups = {"messages", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_validPayload_returns201() { }

    @Test(priority = 2, description = "POST /api/messages with missing content returns 400",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_missingContent_returns400() { }

    @Test(priority = 3, description = "POST /api/messages with non-existent sessionId returns 4xx",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_nonExistentSession_returns4xx() { }

    @Test(priority = 4, description = "POST /api/messages without auth returns 401",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_noAuth_returns401() { }

    @Test(priority = 5, description = "POST /api/messages with empty body returns 400",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void sendMessage_emptyBody_returns400() { }

    // ── GET /api/messages/session/{sessionId} ─────────────────────────────────

    @Test(priority = 6, description = "GET /api/messages/session/{sessionId} returns all messages for the session",
          groups = {"messages", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_validSession_returnsList() { }

    @Test(priority = 7, description = "GET /api/messages/session/{sessionId} for session with no messages returns empty list",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_noMessages_returnsEmptyList() { }

    @Test(priority = 8, description = "GET /api/messages/session/{sessionId} for non-existent session returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySession_nonExistentSession_returns404() { }

    // ── GET /api/messages/sender/{senderId} ───────────────────────────────────

    @Test(priority = 9, description = "GET /api/messages/sender/{senderId} returns all messages sent by user",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySender_validSender_returnsList() { }

    @Test(priority = 10, description = "GET /api/messages/sender/{senderId} for user with no messages returns empty list",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessagesBySender_noMessages_returnsEmptyList() { }

    // ── GET /api/messages/{messageId} ─────────────────────────────────────────

    @Test(priority = 11, description = "GET /api/messages/{messageId} for existing message returns 200",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessageById_existingId_returns200() { }

    @Test(priority = 12, description = "GET /api/messages/{messageId} for non-existent id returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getMessageById_nonExistentId_returns404() { }

    // ── DELETE /api/messages/{messageId} ─────────────────────────────────────

    @Test(priority = 13, description = "DELETE /api/messages/{messageId} by sender returns 200 or 204",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_bySender_returns200Or204() { }

    @Test(priority = 14, description = "DELETE /api/messages/{messageId} by different user returns 403",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_differentUser_returns403() { }

    @Test(priority = 15, description = "DELETE /api/messages/{messageId} for non-existent id returns 404",
          groups = {"messages", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteMessage_nonExistentId_returns404() { }
}
