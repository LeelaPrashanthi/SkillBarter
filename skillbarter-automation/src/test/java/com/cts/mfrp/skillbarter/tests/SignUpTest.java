package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProfileSetupPage;
import com.cts.mfrp.skillbarter.pages.SignUpPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Scenario : TS_003 – Verify Sign Up Functionality
 * Test Cases : TC_015 → TC_020
 */
public class SignUpTest extends BaseTest {

    private SignUpPage signUpPage;

    @BeforeMethod(alwaysRun = true)
    public void openSignUpPage() {
        navigateTo(AppConstants.SIGNUP_URL);
        signUpPage = new SignUpPage(driver);
    }

    @Test(testName = "TC_015", description = "Sign Up with valid data, complete profile setup, lands on dashboard")
    public void tc015_validSignUpCreatesAccount() {
        String uniqueEmail = "testuser+" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        signUpPage.signUp("John Test", uniqueEmail, "Test@1234", "Test@1234");

        ProfileSetupPage profileSetupPage = new ProfileSetupPage(driver);
        profileSetupPage.completeProfileSetup(
            "John Test",
            "Automation test user exploring skill barter."
        );

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("dashboard"),
            "Expected redirect to dashboard after profile setup. URL: " + url
        );
    }

    @Test(testName = "TC_016", description = "Duplicate email shows error")
    public void tc016_duplicateEmailShowsError() {
        signUpPage.signUp("John Test", AppConstants.VALID_EMAIL, "Test@1234", "Test@1234");
        Assert.assertTrue(signUpPage.isErrorMessageDisplayed(), "Duplicate email error not shown");
    }

    @Test(testName = "TC_017", description = "Password mismatch shows error")
    public void tc017_passwordMismatchShowsError() {
        signUpPage.signUp("Jane Test", "jane@test.com", "Test@1234", "Test@4321");
        Assert.assertTrue(signUpPage.isErrorMessageDisplayed(), "Password mismatch error not shown");
    }

    @Test(testName = "TC_018", description = "Empty fields show validation error")
    public void tc018_emptyFieldsValidation() {
        signUpPage.clickSignUp();
        Assert.assertTrue(signUpPage.isErrorMessageDisplayed(), "Empty-field error not shown");
    }

    @Test(testName = "TC_019", description = "Weak password shows error")
    public void tc019_weakPasswordValidation() {
        signUpPage.signUp("Weak Test", "weaktest@test.com", "abc", "abc");
        Assert.assertTrue(signUpPage.isErrorMessageDisplayed(), "Weak password error not shown");
    }

    @Test(testName = "TC_020", description = "Back to Sign In link navigates correctly")
    public void tc020_backToSignInNavigation() {
        Assert.assertTrue(signUpPage.isBackToSignInLinkPresent(), "Sign In link missing");
        signUpPage.clickBackToSignIn();

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("signin") || url.contains("login"),
            "Did not navigate to Sign In. URL: " + url
        );
    }
}
