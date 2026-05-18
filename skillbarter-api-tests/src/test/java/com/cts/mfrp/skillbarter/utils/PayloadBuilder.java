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
        if (name != null)      body.put("name", name);
        if (bio != null)       body.put("bio", bio);
        if (photoUrl != null)  body.put("profilePhotoUrl", photoUrl);
        if (languages != null) body.put("languagesSpoken", languages);
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

    /**
     * Richer user-skill payload that matches the response shape exposed by
     * /api/user-skills/{userId} (isTeach / isLearn). Sends both flag names
     * (`teach`/`isTeach`, `learn`/`isLearn`) so we don't have to guess which
     * one the backend serializer accepts.
     */
    public static Map<String, Object> userSkillPayload(String userId, String skillId,
                                                       boolean isTeach, boolean isLearn) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("skillId", skillId);
        body.put("teach", isTeach);
        body.put("isTeach", isTeach);
        body.put("learn", isLearn);
        body.put("isLearn", isLearn);
        return body;
    }

    // ── Matches ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createMatchPayload(String user1Id, String user2Id, int score) {
        Map<String, Object> body = new HashMap<>();
        body.put("user1Id", user1Id);
        body.put("user2Id", user2Id);
        body.put("matchScore", score);
        return body;
    }

    // ── Sessions ──────────────────────────────────────────────────────────────
    public static Map<String, Object> createSessionPayload(String mentorId, String learnerId, String skillId, String scheduledAt) {
        Map<String, Object> body = new HashMap<>();
        body.put("mentorId", mentorId);
        body.put("learnerId", learnerId);
        body.put("skillId", skillId);
        body.put("scheduledAt", scheduledAt);
        return body;
    }

    // ── Messages ─────────────────────────────────────────────────────────────
    public static Map<String, Object> sendMessagePayload(String sessionId, String senderId, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", sessionId);
        body.put("senderId", senderId);
        body.put("content", content);
        return body;
    }

    // ── Reviews ───────────────────────────────────────────────────────────────
    public static Map<String, Object> submitReviewPayload(String reviewerId, String revieweeId, int rating, String text, String sessionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("reviewerId", reviewerId);
        body.put("revieweeId", revieweeId);
        body.put("rating", rating);
        body.put("reviewText", text);
        body.put("sessionId", sessionId);
        return body;
    }

    // ── Calendar ─────────────────────────────────────────────────────────────
    public static Map<String, Object> createCalendarEventPayload(String userId, String eventDate, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("eventDate", eventDate);
        body.put("description", description);
        return body;
    }

    // ── Transactions ─────────────────────────────────────────────────────────
    public static Map<String, Object> createTransactionPayload(String userId, String sessionId, double amount, String method) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("sessionId", sessionId);
        body.put("amount", amount);
        body.put("paymentMethod", method);
        return body;
    }

    // ── Stories ───────────────────────────────────────────────────────────────
    public static Map<String, Object> createStoryPayload(String userId, String title, String content, String mediaUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("title", title);
        body.put("content", content);
        body.put("mediaUrl", mediaUrl);
        return body;
    }

    private PayloadBuilder() { }
}
