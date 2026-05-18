package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Sign In page.
 * Covers TC_008 – TC_014 (TS_002).
 *
 * Locators verified against the actual SkillBarter Angular login page (/login).
 * The Sign In button is initially disabled and only becomes clickable once the
 * email/password fields pass Angular's form validation.
 */
public class SignInPage {

    private static final Logger log = LogManager.getLogger(SignInPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1")
    private WebElement pageHeading;

    @FindBy(css = "input[type='email']")
    private WebElement emailField;

    @FindBy(css = "input[type='password']")
    private WebElement passwordField;

    // Button has no type='submit'; locate by visible text.
    @FindBy(xpath = "//button[normalize-space()='Sign In']")
    private WebElement signInBtn;

    @FindBy(css = ".error-box, [class*='error'], [role='alert']")
    private WebElement errorMessage;

    @FindBy(css = "a[href*='forgot']")
    private WebElement forgotPasswordLink;

    // Page currently has no Google sign-in button; selector left defensive
    // so existing TC_011 doesn't NPE.
    @FindBy(css = "button[class*='google'], [class*='google-signin'], a[class*='google']")
    private WebElement googleSignInBtn;

    @FindBy(css = "a[href*='signup']")
    private WebElement signUpLink;

    public SignInPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageHeading() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageHeading)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void enterEmail(String email) {
        clearAndType(emailField, email, "email");
    }

    public void enterPassword(String password) {
        clearAndType(passwordField, password, "password");
    }

    public void clickSignIn() {
        click(signInBtn, "Sign In button");
    }

    public void signIn(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink, "Forgot Password link");
    }

    public void clickGoogleSignIn() {
        click(googleSignInBtn, "Google Sign In button");
    }

    public void clickSignUpLink() {
        click(signUpLink, "Sign Up link");
    }

    public boolean isSignUpLinkPresent() {
        return isDisplayed(signUpLink);
    }

    public boolean isForgotPasswordLinkPresent() {
        return isDisplayed(forgotPasswordLink);
    }

    public boolean isGoogleSignInPresent() {
        return isDisplayed(googleSignInBtn);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void clearAndType(WebElement el, String text, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
            log.debug("Typed '{}' into {}", text, name);
        } catch (Exception e) {
            log.warn("clearAndType failed on '{}': {}", name, e.getMessage());
        }
    }

    private void click(WebElement el, String name) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
            log.debug("Clicked: {}", name);
        } catch (Exception e) {
            log.warn("Click failed on '{}': {}", name, e.getMessage());
        }
    }

    private boolean isDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}