package com.cts.mfrp.skillbarter.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * PayloadBuilder – factory for request body Maps / POJOs.
 * Each method returns a Map<String,Object> ready for .body() in REST Assured.
 */
public class PayloadBuilder {

    // ── Auth ──────────────────────────────────────────────────────────────────
    public static Map<String, Object> registerPayload(String name, String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    public static Map<String, Object> loginPayload(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    public static Map<String, Object> forgotPasswordPayload(String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        return body;
    }

    public static Map<String, Object> resetPasswordPayload(String token, String newPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("newPassword", newPassword);
        return body;
    }

    // ── Users ─────────────────────────────────────────────────────────────────
    public static Map<String, Object> updateUserPayload(String name, String bio, String photoUrl, String languages) {
        Map<String, Object> body = new HashMap<>();
        if (name != null) body.put("name", name);
        if (bio != null) body.put("bio", bio);
        if (photoUrl != null) body.put("photoUrl", photoUrl);
        if (languages != null) body.put("languages", languages);
        return body;
    }

    // ── Skills ────────────────────────────────────────────────────────────────
    public static Map<String, Object> userSkillPayload(String userId, String skillId, boolean teach) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("skillId", skillId);
        body.put("teach", teach);
        return body;
    }

    // ── Matches ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createMatchPayload(String user1Id, String user2Id, int score) {
        Map<String, Object> body = new HashMap<>();
        body.put("user1", user1Id);
        body.put("user2", user2Id);
        body.put("score", score);
        return body;
    }

    // ── Sessions ──────────────────────────────────────────────────────────────
    public static Map<String, Object> createSessionPayload(String mentorId, String learnerId, String skillId, String scheduledAt) {
        Map<String, Object> body = new HashMap<>();
        body.put("mentor", refUser(mentorId));
        body.put("learner", refUser(learnerId));
        body.put("skill", refSkill(skillId));
        body.put("scheduledAt", scheduledAt);
        return body;
    }

    // ── Messages ─────────────────────────────────────────────────────────────
    public static Map<String, Object> sendMessagePayload(String sessionId, String senderId, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("session", sessionId);
        body.put("sender", senderId);
        body.put("content", content);
        return body;
    }

    // ── Reviews ───────────────────────────────────────────────────────────────
    public static Map<String, Object> submitReviewPayload(String reviewerId, String revieweeId, int rating, String text, String sessionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("reviewer", reviewerId);
        body.put("reviewee", revieweeId);
        body.put("rating", rating);
        body.put("text", text);
        body.put("session", sessionId);
        return body;
    }

    // ── Calendar ─────────────────────────────────────────────────────────────
    public static Map<String, Object> createCalendarEventPayload(String userId, String eventDate, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("user", userId);
        body.put("eventDate", eventDate);
        body.put("description", description);
        return body;
    }

    // ── Transactions ─────────────────────────────────────────────────────────
    public static Map<String, Object> createTransactionPayload(String userId, String sessionId, double amount, String method) {
        return createTransactionPayload(userId, sessionId, amount, method, "Pending");
    }

    public static Map<String, Object> createTransactionPayload(String userId, String sessionId, double amount, String method, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("user", refUser(userId));
        body.put("session", refSession(sessionId));
        body.put("amount", amount);
        body.put("paymentMethod", method);
        body.put("status", status);
        return body;
    }

    // ── reference helpers (Spring Data style nested ID objects) ──────────────
    private static Map<String, Object> refUser(String userId) {
        Map<String, Object> m = new HashMap<>();
        if (userId != null) m.put("userId", parseLongOrString(userId));
        return m;
    }

    private static Map<String, Object> refSkill(String skillId) {
        Map<String, Object> m = new HashMap<>();
        if (skillId != null) m.put("skillId", parseLongOrString(skillId));
        return m;
    }

    private static Map<String, Object> refSession(String sessionId) {
        Map<String, Object> m = new HashMap<>();
        if (sessionId != null) m.put("sessionId", parseLongOrString(sessionId));
        return m;
    }

    private static Object parseLongOrString(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return s; }
    }

    // ── Stories ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createStoryPayload(String userId, String title, String content, String mediaUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("user", refUser(userId));
        body.put("title", title);
        body.put("content", content);
        body.put("mediaUrl", mediaUrl);
        return body;
    }

    private PayloadBuilder() { }
}
