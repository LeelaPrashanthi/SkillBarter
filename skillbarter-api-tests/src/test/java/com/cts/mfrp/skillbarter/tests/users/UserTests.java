package com.cts.mfrp.skillbarter.tests.users;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * UserTests – covers all /api/users endpoints.
 *
 * Scenario  : TS_USERS
 * Endpoints : GET  /api/users
 *             GET  /api/users/{id}
 *             GET  /api/users/search?name=X
 *             PUT  /api/users/{id}
 *             PATCH /api/users/{id}/password
 *             PATCH /api/users/{id}/xp?points=N
 *             POST /api/users/{id}/photo
 *             DELETE /api/users/{id}
 */
public class UserTests extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSecondUser();
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test(priority = 1, description = "GET /api/users with valid token returns 200 and non-empty list",
          groups = {"users", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getAllUsers_withAuth_returns200AndList() { }

    @Test(priority = 2, description = "GET /api/users without token returns 401",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getAllUsers_noAuth_returns401() { }

    // ── GET /api/users/{id} ───────────────────────────────────────────────────

    @Test(priority = 3, description = "GET /api/users/{id} for existing user returns 200 and user object",
          groups = {"users", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getUserById_existingId_returns200() { }

    @Test(priority = 4, description = "GET /api/users/{id} for non-existent id returns 404",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getUserById_nonExistentId_returns404() { }

    @Test(priority = 5, description = "GET /api/users/{id} without token returns 401",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void getUserById_noAuth_returns401() { }

    // ── GET /api/users/search?name=X ─────────────────────────────────────────

    @Test(priority = 6, description = "GET /api/users/search?name=X returns matching users case-insensitively",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void searchUserByName_validQuery_returnsMatches() { }

    @Test(priority = 7, description = "GET /api/users/search?name=X with no match returns empty list",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void searchUserByName_noMatch_returnsEmptyList() { }

    @Test(priority = 8, description = "GET /api/users/search without name param returns 400",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void searchUserByName_missingParam_returns400() { }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test(priority = 9, description = "PUT /api/users/{id} with valid payload updates and returns 200",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateUser_validPayload_returns200() { }

    @Test(priority = 10, description = "PUT /api/users/{id} by another user returns 403",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateUser_differentUser_returns403() { }

    @Test(priority = 11, description = "PUT /api/users/{id} without token returns 401",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updateUser_noAuth_returns401() { }

    // ── PATCH /api/users/{id}/password ───────────────────────────────────────

    @Test(priority = 12, description = "PATCH /api/users/{id}/password with valid new password returns 200",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updatePassword_validPayload_returns200() { }

    @Test(priority = 13, description = "PATCH /api/users/{id}/password with missing field returns 400",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void updatePassword_missingField_returns400() { }

    // ── PATCH /api/users/{id}/xp?points=N ────────────────────────────────────

    @Test(priority = 14, description = "PATCH /api/users/{id}/xp?points=N adds XP and returns updated user",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void addXp_positivePoints_returnsUpdatedUser() { }

    @Test(priority = 15, description = "PATCH /api/users/{id}/xp without points param returns 400",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void addXp_missingPointsParam_returns400() { }

    // ── POST /api/users/{id}/photo ────────────────────────────────────────────

    @Test(priority = 16, description = "POST /api/users/{id}/photo with valid image returns 200 and photoUrl",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void uploadProfilePhoto_validImage_returns200AndPhotoUrl() { }

    @Test(priority = 17, description = "POST /api/users/{id}/photo with no file returns 400",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void uploadProfilePhoto_noFile_returns400() { }

    // ── DELETE /api/users/{id} ────────────────────────────────────────────────

    @Test(priority = 18, description = "DELETE /api/users/{id} by owner returns 200 or 204",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteUser_byOwner_returns200Or204() { }

    @Test(priority = 19, description = "DELETE /api/users/{id} by different user returns 403",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteUser_differentUser_returns403() { }

    @Test(priority = 20, description = "DELETE /api/users/{id} for already-deleted user returns 404",
          groups = {"users", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    public void deleteUser_nonExistentId_returns404() { }
}
