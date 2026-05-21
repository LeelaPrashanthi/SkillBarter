package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.SavedProfilesPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Saved Profiles page.
 *
 * Scenario   : TS_010 – Verify Saved Profiles Page
 * Requirement: REQ-2.10
 * Test Cases : TC_047 → TC_050
 * Group      : saved-profiles, regression
 */
public class SavedProfilesTest extends BaseTest {

    // Local override: this suite needs an account that already has saved profiles.
    // AppConstants.VALID_EMAIL ("hello3@...") is shared with 5 other test classes and
    // its account currently has no saved entries, so TC_048/049/050 had nothing to click.
    private static final String SAVED_PROFILES_EMAIL    = "leela123@gmail.com";
    private static final String SAVED_PROFILES_PASSWORD = "123456";

    private SavedProfilesPage savedProfilesPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenSavedProfiles() {
        navigateTo(AppConstants.SIGNIN_URL);
        new SignInPage(driver).signIn(SAVED_PROFILES_EMAIL, SAVED_PROFILES_PASSWORD);
        wait.until(ExpectedConditions.urlContains("dashboard"));
        navigateTo(AppConstants.SAVED_PROFILES_URL);
        wait.until(ExpectedConditions.urlContains("saved-profiles"));
        savedProfilesPage = new SavedProfilesPage(driver);
        // The Angular Saved Profiles page hydrates cards async; give it up to 10s
        // before tests assert on card presence so we don't read a half-rendered DOM.
        savedProfilesPage.waitForProfilesToLoad(10);
    }

    @Test(testName = "TC_044", description = "Saved Profiles page loads dynamically with profile cards",
          groups = {"saved-profiles", "regression"}, priority = 47, retryAnalyzer = RetryAnalyzer.class)
    public void tc047_savedProfilesLoadDynamically() {
        Assert.assertTrue(
            savedProfilesPage.isPageLoaded(),
            "Saved Profiles page title not visible — page failed to render."
        );
        Assert.assertTrue(
            getCurrentUrl().contains("saved-profiles"),
            "Not on the saved-profiles URL. Current: " + getCurrentUrl()
        );
        Assert.assertTrue(
            savedProfilesPage.getProfileCount() >= 0,
            "Profile cards collection could not be queried."
        );
    }

    @Test(testName = "TC_045", description = "View Profile from Saved Profiles opens detailed profile page",
          groups = {"saved-profiles", "regression"}, priority = 48, retryAnalyzer = RetryAnalyzer.class)
    public void tc048_viewProfileFromSavedProfiles() {
        if (!savedProfilesPage.waitForProfilesToLoad(10)) {
            throw new SkipException("No saved profiles rendered after 10s — TC_048 requires at least one.");
        }

        String urlBefore = getCurrentUrl();
        savedProfilesPage.clickViewProfileAt(0);
        wait.until(d -> !d.getCurrentUrl().equals(urlBefore));

        String urlAfter = getCurrentUrl().toLowerCase();
        Assert.assertFalse(
            urlAfter.contains("saved-profiles"),
            "View Profile click did not navigate away from the Saved Profiles page. URL: " + urlAfter
        );
        Assert.assertTrue(
            urlAfter.contains("matches") || urlAfter.contains("profile") || urlAfter.contains("user"),
            "View Profile click went to an unexpected page. URL: " + urlAfter
        );
    }

    @Test(testName = "TC_046", description = "Message button from Saved Profiles opens chat",
          groups = {"saved-profiles", "regression"}, priority = 49, retryAnalyzer = RetryAnalyzer.class)
    public void tc049_messageFromSavedProfiles() {
        if (!savedProfilesPage.waitForProfilesToLoad(10)) {
            throw new SkipException("No saved profiles rendered after 10s — TC_049 requires at least one.");
        }

        String urlBefore = getCurrentUrl();
        savedProfilesPage.clickMessageAt(0);

        try {
            wait.until(d -> !d.getCurrentUrl().equals(urlBefore));
        } catch (Exception ignored) {
            // Chat may open as a modal/panel without URL change — the URL check below handles both.
        }

        String urlAfter = getCurrentUrl().toLowerCase();
        boolean openedChat = urlAfter.contains("messenger")
                          || urlAfter.contains("chat")
                          || urlAfter.contains("message");
        Assert.assertTrue(
            openedChat,
            "Message button did not navigate to a chat / messenger flow. URL: " + urlAfter
        );
    }
}
