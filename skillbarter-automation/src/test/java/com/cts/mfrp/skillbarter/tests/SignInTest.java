package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Sign In functionality.
 *
 * Scenario : TS_002 – Verify Sign In Functionality
 * Requirement: REQ-2.2
 * Test Cases : TC_008 → TC_014
 * Group      : sign-in, regression, smoke
 */
public class  SignInTest extends BaseTest {

    private SignInPage signInPage;

    @BeforeMethod(alwaysRun = true)
    public void openSignInPage() {
        navigateTo(AppConstants.SIGNIN_URL);
        signInPage = new SignInPage(driver);
    }

    @Test(testName = "TC_008", description = "Sign In with valid credentials redirects to dashboard",
          groups = {"sign-in", "smoke", "regression"}, priority = 8, retryAnalyzer = RetryAnalyzer.class)
    public void tc008_validLoginRedirectsToDashboard() {
        signInPage.signIn(AppConstants.VALID_EMAIL, AppConstants.VALID_PASSWORD);

        wait.until(ExpectedConditions.urlContains("dashboard"));

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("dashboard"),
            "Expected redirect to dashboard after valid sign-in. Actual URL: " + url
        );
    }

    @Test(testName = "TC_009", description = "Sign In with invalid credentials shows error message",
          groups = {"sign-in", "regression"}, priority = 9, retryAnalyzer = RetryAnalyzer.class)
    public void tc009_invalidCredentialsShowError() {
        signInPage.signIn("not-a-real-user@test.com", "WrongPass@123");

        Assert.assertTrue(
            signInPage.isErrorMessageDisplayed(),
            "Expected an error message for invalid credentials, but none was shown."
        );
        Assert.assertFalse(
            getCurrentUrl().contains("dashboard"),
            "Sign-in should not redirect to dashboard for invalid credentials. Actual URL: " + getCurrentUrl()
        );
    }

    @Test(testName = "TC_010", description = "Forgot Password link redirects to password recovery page",
          groups = {"sign-in", "regression"}, priority = 10, retryAnalyzer = RetryAnalyzer.class)
    public void tc010_forgotPasswordRedirect() {
        Assert.assertTrue(
            signInPage.isForgotPasswordLinkPresent(),
            "Forgot Password link is not visible on the Sign In page."
        );

        signInPage.clickForgotPassword();
        wait.until(ExpectedConditions.urlContains("forgot-password"));

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("forgot-password"),
            "Expected redirect to the password recovery page. Actual URL: " + url
        );
    }



    @Test(testName = "TC_012", description = "Clicking Sign Up link from Sign In page navigates to registration",
          groups = {"sign-in", "regression"}, priority = 12, retryAnalyzer = RetryAnalyzer.class)
    public void tc012_navigateToSignUpFromSignIn() {
        Assert.assertTrue(
            signInPage.isSignUpLinkPresent(),
            "Sign Up link is not visible on the Sign In page."
        );

        signInPage.clickSignUpLink();
        wait.until(ExpectedConditions.urlContains("signup"));

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("signup"),
            "Expected redirect to the Sign Up page. Actual URL: " + url
        );
    }

    @Test(testName = "TC_013", description = "Invalid email format shows validation error on Sign In",
          groups = {"sign-in", "regression"}, priority = 13, retryAnalyzer = RetryAnalyzer.class)
    public void tc013_emailFormatValidation() {
        signInPage.enterEmail("not-an-email");
        signInPage.enterPassword("Test@1234");
        signInPage.clickSignIn();

        boolean nativeInvalid = Boolean.TRUE.equals(executeScript(
            "var e = document.querySelector('input[type=email]');" +
            "return e && !e.validity.valid;"
        ));
        boolean appError = !nativeInvalid && signInPage.isErrorMessageDisplayed();

        Assert.assertTrue(
            nativeInvalid || appError,
            "Expected an email-format validation error (native or app-level), got none."
        );
        Assert.assertFalse(
            getCurrentUrl().contains("dashboard"),
            "Sign-in should not redirect to dashboard for an invalid email format. URL: " + getCurrentUrl()
        );
    }

    @Test(testName = "TC_014", description = "Submitting empty email and password fields shows required field error",
          groups = {"sign-in", "regression"}, priority = 14, retryAnalyzer = RetryAnalyzer.class)
    public void tc014_emptyFieldsValidation() {
        signInPage.clickSignIn();

        boolean nativeInvalid = Boolean.TRUE.equals(executeScript(
            "var e = document.querySelector('input[type=email]');" +
            "var p = document.querySelector('input[type=password]');" +
            "return (e && !e.validity.valid) || (p && !p.validity.valid);"
        ));
        boolean appError = !nativeInvalid && signInPage.isErrorMessageDisplayed();

        Assert.assertTrue(
            nativeInvalid || appError,
            "Expected a required-field validation error for empty submission, got none."
        );
        Assert.assertFalse(
            getCurrentUrl().contains("dashboard"),
            "Sign-in should not redirect to dashboard with empty fields. URL: " + getCurrentUrl()
        );
    }
}
