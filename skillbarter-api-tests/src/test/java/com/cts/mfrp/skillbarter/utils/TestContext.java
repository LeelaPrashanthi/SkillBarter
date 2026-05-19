package com.cts.mfrp.skillbarter.utils;

import org.testng.SkipException;

/**
 * TestContext – thread-safe shared state bag for IDs and tokens
 * produced during the test run and consumed by later tests.
 */
public class TestContext {

    public static volatile String authToken;
    public static volatile String secondUserToken;

    public static volatile String registeredUserId;
    public static volatile String secondUserId;

    public static volatile String skillId;
    public static volatile String userSkillId;

    public static volatile String matchId;
    public static volatile String sessionId;
    public static volatile String messageId;
    public static volatile String notificationId;
    public static volatile String reviewId;
    public static volatile String reviewReviewerId;
    public static volatile String reviewSessionId;
    public static volatile String calendarEventId;
    public static volatile String transactionId;
    public static volatile String storyId;

    public static volatile String resetToken;

    public static void requireAuth() {
        if (authToken == null || registeredUserId == null) {
            throw new SkipException("Auth not initialised — Bootstrap.ensureFirstUser() must run first");
        }
    }

    public static void requireSecondUser() {
        if (secondUserToken == null || secondUserId == null) {
            throw new SkipException("Second user not initialised — Bootstrap.ensureSecondUser() must run first");
        }
    }

    public static void requireSession() {
        if (sessionId == null) {
            throw new SkipException("Session not initialised — Bootstrap.ensureSession() must run first");
        }
    }

    private TestContext() { }
}
