package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.pages.UserProfilePage;
import com.cts.mfrp.skillbarter.utils.ConfigReader;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for User Profile page (edit & view).
 *
 * Scenario   : TS_009 – Verify User Profile Edit and View Functions
 * Requirement: REQ-2.9
 * Test Cases : TC_043 → TC_046
 * Group      : user-profile, regression
 *
 * Notes:
 *  - TC_045 was originally "View My Public Profile". The live app has no such
 *    link; the topbar dropdown only contains Profile / Saved Profiles / Log Out.
 *    The test was redirected to "Profile" item with a comment for the test lead.
 *  - TC_046 verifies Save button is disabled when Name is empty (real Angular
 *    validation signal), not a popup error message.
 */
public class UserProfileTest extends BaseTest {

    private UserProfilePage userProfilePage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenProfile() {
        String baseUrl = ConfigReader.getBaseUrl().replaceAll("/+$", "");

        navigateTo(baseUrl + "/login");

        SignInPage signInPage = new SignInPage(driver);
        signInPage.signIn(ConfigReader.getValidEmail(), ConfigReader.getValidPassword());

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        navigateTo(baseUrl + "/app/profile");

        userProfilePage = new UserProfilePage(driver);
        Assert.assertTrue(userProfilePage.isPageLoaded(),
                "User Profile page failed to load after login. Current URL: " + driver.getCurrentUrl());
    }

    @Test(testName = "TC_040", description = "Editing and saving profile updates the information",
            groups = {"user-profile", "regression"}, priority = 43, retryAnalyzer = RetryAnalyzer.class)
    public void tc043_editAndSaveProfile() {
        userProfilePage.editProfile(
                "Test User Updated",
                "Updated profile description for automation run.",
                "Java",
                "Figma");

        userProfilePage.clickSaveProfile();

        Assert.assertTrue(userProfilePage.isSuccessMessageDisplayed(),
                "Expected a save confirmation after editing profile, but none was shown.");
    }

    @Test(testName = "TC_041", description = "Change Avatar uploads new image and updates preview",
            groups = {"user-profile", "regression"}, priority = 44, retryAnalyzer = RetryAnalyzer.class)
    public void tc044_changeAvatarUpdatesPreview() {
        String imagePath = new File("src/test/resources/testdata/test_avatar.png").getAbsolutePath();

        userProfilePage.changeAvatar(imagePath);

        Assert.assertTrue(userProfilePage.isAvatarImageDisplayed(),
                "Avatar preview did not update after upload.");

        // App shows a permanent "Profile photo updated successfully" line.
        Assert.assertTrue(userProfilePage.isSuccessMessageDisplayed(),
                "Expected an avatar-upload confirmation message, but none was shown.");
    }

    @Test(testName = "TC_042", description = "User dropdown Profile link opens the profile page",
            groups = {"user-profile", "regression"}, priority = 45, retryAnalyzer = RetryAnalyzer.class)
    public void tc045_viewPublicProfile() {
        // NOTE: TC_045 doc says "View My Public Profile" but live app's topbar
        // dropdown only has Profile / Saved Profiles / Log Out. We exercise
        // the Profile link instead. Flag to test lead for doc update.
        userProfilePage.clickProfileFromDropdown();

        Assert.assertTrue(userProfilePage.isProfilePageOpenedFromDropdown(),
                "Clicking Profile in the topbar dropdown did not open the profile page.");

        Assert.assertTrue(userProfilePage.isPageLoaded(),
                "Profile page did not load after using the dropdown link.");
    }

    @Test(testName = "TC_043", description = "Save Profile is disabled when mandatory Name field is empty",
            groups = {"user-profile", "regression"}, priority = 46, retryAnalyzer = RetryAnalyzer.class)
    public void tc046_emptyMandatoryFieldShowsError() {
        // App uses Angular form validation: when Name is empty (mandatory field),
        // the Save Profile button becomes disabled rather than showing a popup error.
        userProfilePage.clearName();

        Assert.assertTrue(userProfilePage.isSaveButtonDisabled(),
                "Save Profile button should be disabled when mandatory Name field is empty.");
    }
}