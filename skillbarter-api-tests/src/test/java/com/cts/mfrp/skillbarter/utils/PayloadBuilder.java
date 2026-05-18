package com.cts.mfrp.skillbarter.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PayloadBuilder – factory for request body Maps / POJOs.
 * Each method returns a Map<String,Object> ready for .body() in REST Assured.
 */
public class PayloadBuilder {

    public static Map<String, Object> registerPayload(String name, String email, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    public static Map<String, Object> loginPayload(String email, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
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
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", token);
        body.put("newPassword", newPassword);
        return body;
    }

    public static Map<String, Object> updateUserPayload(String name, String bio, String photoUrl, String languages) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (name != null) body.put("name", name);
        if (bio != null) body.put("bio", bio);
        if (photoUrl != null) body.put("profilePhotoUrl", photoUrl);
        if (languages != null) body.put("languagesSpoken", languages);
        return body;
    }

    public static Map<String, Object> userSkillPayload(String userId, String skillId, boolean teach) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("skillId", skillId);
        body.put("teach", teach);
        return body;
    }

    public static Map<String, Object> createMatchPayload(String user1Id, String user2Id, int score) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user1Id", user1Id);
        body.put("user2Id", user2Id);
        body.put("score", score);
        return body;
    }

    public static Map<String, Object> createSessionPayload(String mentorId, String learnerId, String skillId, String scheduledAt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mentorId", mentorId);
        body.put("learnerId", learnerId);
        body.put("skillId", skillId);
        body.put("scheduledAt", scheduledAt);
        return body;
    }

    public static Map<String, Object> sendMessagePayload(String sessionId, String senderId, String content) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sessionId", sessionId);
        body.put("senderId", senderId);
        body.put("content", content);
        return body;
    }

    public static Map<String, Object> submitReviewPayload(String reviewerId, String revieweeId, int rating, String text, String sessionId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reviewerId", reviewerId);
        body.put("revieweeId", revieweeId);
        body.put("rating", rating);
        body.put("text", text);
        body.put("sessionId", sessionId);
        return body;
    }

    public static Map<String, Object> createCalendarEventPayload(String userId, String eventDate, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (userId != null) {
            Map<String, Object> userRef = new LinkedHashMap<>();
            userRef.put("id", coerceId(userId));
            body.put("user", userRef);
        }
        if (eventDate != null) body.put("eventDate", eventDate);
        if (description != null) body.put("description", description);
        return body;
    }

    public static Map<String, Object> createCalendarEventPayloadFlat(String userId, String eventDate, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (userId != null) body.put("userId", coerceId(userId));
        if (eventDate != null) body.put("eventDate", eventDate);
        if (description != null) body.put("description", description);
        return body;
    }

    public static Map<String, Object> createCalendarEventPayloadUserRaw(String userId, String eventDate, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (userId != null) body.put("user", coerceId(userId));
        if (eventDate != null) body.put("eventDate", eventDate);
        if (description != null) body.put("description", description);
        return body;
    }

    public static Map<String, Object> createCalendarEventPayloadNoUser(String eventDate, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (eventDate != null) body.put("eventDate", eventDate);
        if (description != null) body.put("description", description);
        return body;
    }

    public static Map<String, Object> createCalendarEventPayloadEventDateTime(String userId, String eventDate, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (userId != null) {
            Map<String, Object> userRef = new LinkedHashMap<>();
            userRef.put("id", coerceId(userId));
            body.put("user", userRef);
        }
        if (eventDate != null) body.put("eventDateTime", eventDate);
        if (description != null) body.put("description", description);
        return body;
    }

    private static Object coerceId(String id) {
        try { return Long.parseLong(id); } catch (NumberFormatException e) { return id; }
    }

    public static Map<String, Object> createTransactionPayload(String userId, String sessionId, double amount, String method) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("sessionId", sessionId);
        body.put("amount", amount);
        body.put("method", method);
        return body;
    }

    public static Map<String, Object> createStoryPayload(String userId, String title, String content, String mediaUrl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("title", title);
        body.put("content", content);
        body.put("mediaUrl", mediaUrl);
        return body;
    }

    public static Map<String, Object> updatePasswordPayload(String currentPassword, String newPassword) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);
        return body;
    }

    private PayloadBuilder() { }
}
