package com.cts.mfrp.skillbarter.utils;

import org.testng.SkipException;

/**
 * TestContext – thread-safe shared state bag for IDs and tokens
 * produced during the test run and consumed by later tests.
 *
 * Fields are populated by Bootstrap helpers and read by test methods.
 */
public class TestContext {

    // ── Auth tokens ───────────────────────────────────────────────────────────
    public static String authToken;
    public static String secondUserToken;

    // ── User IDs ──────────────────────────────────────────────────────────────
    public static String registeredUserId;
    public static String secondUserId;

    // ── Skill IDs ─────────────────────────────────────────────────────────────
    public static String skillId;
    public static String userSkillId;

    // ── Match IDs ─────────────────────────────────────────────────────────────
    public static String matchId;

    // ── Session IDs ───────────────────────────────────────────────────────────
    public static String sessionId;

    // ── Message IDs ───────────────────────────────────────────────────────────
    public static String messageId;

    // ── Notification IDs ─────────────────────────────────────────────────────
    public static String notificationId;

    // ── Review IDs ───────────────────────────────────────────────────────────
    public static String reviewId;
    public static String reviewReviewerId;   // userId of the reviewer on the seeded review
    public static String reviewSessionId;    // sessionId tied to the seeded review

    // ── Calendar Event IDs ───────────────────────────────────────────────────
    public static String calendarEventId;

    // ── Transaction IDs ──────────────────────────────────────────────────────
    public static String transactionId;

    // ── Story IDs ────────────────────────────────────────────────────────────
    public static String storyId;

    // ── Guards ───────────────────────────────────────────────────────────────
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
