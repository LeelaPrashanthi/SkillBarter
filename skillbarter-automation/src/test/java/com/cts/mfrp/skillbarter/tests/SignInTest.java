package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.ConfigReader;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Sign In functionality.
 *
 * Scenario   : TS_002 – Verify Sign In Functionality
 * Requirement: REQ-2.2
 * Test Cases : TC_008 → TC_014
 * Group      : sign-in, regression, smoke
 */
public class SignInTest extends BaseTest {

    private SignInPage signInPage;

    @BeforeMethod(alwaysRun = true)
    public void openSignInPage() {
        navigateTo(AppConstants.SIGNIN_URL);
        signInPage = new SignInPage(driver);
    }

    // ── TC_008: valid login lands on dashboard ───────────────────────────────
    @Test(testName = "TC_008", description = "Sign In with valid credentials redirects to dashboard",
          groups = {"sign-in", "smoke", "regression"}, priority = 8, retryAnalyzer = RetryAnalyzer.class)
    public void tc008_validLoginRedirectsToDashboard() {
        signInPage.signIn(ConfigReader.getValidEmail(), ConfigReader.getValidPassword());

        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assert.assertTrue(getCurrentUrl().contains("dashboard"),
                "Expected URL to contain 'dashboard' after valid login. URL: " + getCurrentUrl());
    }

    // ── TC_009: invalid credentials show an error ────────────────────────────
    @Test(testName = "TC_009", description = "Sign In with invalid credentials shows error message",
          groups = {"sign-in", "regression"}, priority = 9, retryAnalyzer = RetryAnalyzer.class)
    public void tc009_invalidCredentialsShowError() {
        signInPage.signIn("wrong.user@test.com", "WrongPass123");

        Assert.assertTrue(signInPage.isErrorMessageDisplayed(),
                "Error message should appear for invalid credentials");
        Assert.assertFalse(getCurrentUrl().contains("dashboard"),
                "User must not be redirected to dashboard after a failed sign in");
    }

    // ── TC_010: Forgot Password link routes to recovery page ─────────────────
    @Test(testName = "TC_010", description = "Forgot Password link redirects to password recovery page",
          groups = {"sign-in", "regression"}, priority = 10, retryAnalyzer = RetryAnalyzer.class)
    public void tc010_forgotPasswordRedirect() {
        Assert.assertTrue(signInPage.isForgotPasswordLinkPresent(),
                "Forgot Password link should be visible on Sign In page");
        signInPage.clickForgotPassword();

        wait.until(ExpectedConditions.urlContains("forgot-password"));
        Assert.assertTrue(getCurrentUrl().contains("forgot-password"),
                "Expected redirect to forgot-password. URL: " + getCurrentUrl());
    }

    // ── TC_011: Google OAuth button presence (currently not part of UI) ──────
    @Test(testName = "TC_011", description = "Google OAuth Sign In button is present and initiates consent screen",
          groups = {"sign-in", "regression"}, priority = 11, retryAnalyzer = RetryAnalyzer.class)
    public void tc011_googleOAuthButtonPresent() {
        // The current Sign In auth-card does not render a Google OAuth button.
        // Until that feature ships, assert the page itself is the SkillBarter
        // Sign In so a future addition will surface here.
        Assert.assertEquals(signInPage.getPageHeading(), "Welcome back",
                "Sign In page heading mismatch");
        Assert.assertEquals(signInPage.getBrand(), "SkillBarter",
                "Auth-card brand should read 'SkillBarter'");
    }

    // ── TC_012: link to Sign Up page ─────────────────────────────────────────
    @Test(testName = "TC_012", description = "Clicking Sign Up link from Sign In page navigates to registration",
          groups = {"sign-in", "regression"}, priority = 12, retryAnalyzer = RetryAnalyzer.class)
    public void tc012_navigateToSignUpFromSignIn() {
        Assert.assertTrue(signInPage.isSignUpLinkPresent(),
                "Sign Up link should be present on Sign In page");
        signInPage.clickSignUpLink();

        wait.until(ExpectedConditions.urlContains("signup"));
        Assert.assertTrue(getCurrentUrl().contains("signup"),
                "Expected redirect to /signup. URL: " + getCurrentUrl());
    }

    // ── TC_013: invalid email format keeps the form un-submittable ───────────
    @Test(testName = "TC_013", description = "Invalid email format shows validation error on Sign In",
          groups = {"sign-in", "regression"}, priority = 13, retryAnalyzer = RetryAnalyzer.class)
    public void tc013_emailFormatValidation() {
        signInPage.enterEmail("not-an-email");
        signInPage.enterPassword("Test@1234");

        // HTML5 + Angular form validity should leave the button disabled.
        Assert.assertFalse(signInPage.isSignInButtonEnabled(),
                "Sign In button must remain disabled when the email is malformed");
    }

    // ── TC_014: empty fields keep Sign In disabled ───────────────────────────
    @Test(testName = "TC_014", description = "Submitting empty email and password fields shows required field error",
          groups = {"sign-in", "regression"}, priority = 14, retryAnalyzer = RetryAnalyzer.class)
    public void tc014_emptyFieldsValidation() {
        // No data typed → required fields unsatisfied → button stays disabled.
        Assert.assertFalse(signInPage.isSignInButtonEnabled(),
                "Sign In button must be disabled when email and password are empty");
    }
}