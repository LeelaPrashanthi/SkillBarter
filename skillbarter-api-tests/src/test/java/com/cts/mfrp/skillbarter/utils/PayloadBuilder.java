package com.cts.mfrp.skillbarter.utils;

import java.util.Map;

/**
 * PayloadBuilder – factory for request body Maps / POJOs.
 * Each method returns a Map<String,Object> ready for .body() in REST Assured.
 */
public class PayloadBuilder {

    // ── Auth ──────────────────────────────────────────────────────────────────
    public static Map<String, Object> registerPayload(String name, String email, String password) { return null; }
    public static Map<String, Object> loginPayload(String email, String password) { return null; }
    public static Map<String, Object> forgotPasswordPayload(String email) { return null; }
    public static Map<String, Object> resetPasswordPayload(String token, String newPassword) { return null; }

    // ── Users ─────────────────────────────────────────────────────────────────
    public static Map<String, Object> updateUserPayload(String name, String bio, String photoUrl, String languages) { return null; }

    // ── Skills ────────────────────────────────────────────────────────────────
    public static Map<String, Object> userSkillPayload(String userId, String skillId, boolean teach) { return null; }

    // ── Matches ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createMatchPayload(String user1Id, String user2Id, int score) { return null; }

    // ── Sessions ──────────────────────────────────────────────────────────────
    public static Map<String, Object> createSessionPayload(String mentorId, String learnerId, String skillId, String scheduledAt) { return null; }

    // ── Messages ─────────────────────────────────────────────────────────────
    public static Map<String, Object> sendMessagePayload(String sessionId, String senderId, String content) { return null; }

    // ── Reviews ───────────────────────────────────────────────────────────────
    public static Map<String, Object> submitReviewPayload(String reviewerId, String revieweeId, int rating, String text, String sessionId) { return null; }

    // ── Calendar ─────────────────────────────────────────────────────────────
    public static Map<String, Object> createCalendarEventPayload(String userId, String eventDate, String description) { return null; }

    // ── Transactions ─────────────────────────────────────────────────────────
    public static Map<String, Object> createTransactionPayload(String userId, String sessionId, double amount, String method) { return null; }

    // ── Stories ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createStoryPayload(String userId, String title, String content, String mediaUrl) { return null; }

    private PayloadBuilder() { }
}
