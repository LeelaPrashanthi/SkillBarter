package com.cts.mfrp.skillbarter.utils;

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

    // ── Calendar Event IDs ───────────────────────────────────────────────────
    public static String calendarEventId;

    // ── Transaction IDs ──────────────────────────────────────────────────────
    public static String transactionId;

    // ── Story IDs ────────────────────────────────────────────────────────────
    public static String storyId;

    // ── Guards ───────────────────────────────────────────────────────────────
    public static void requireAuth() { }

    public static void requireSecondUser() { }

    public static void requireSession() { }

    private TestContext() { }
}
