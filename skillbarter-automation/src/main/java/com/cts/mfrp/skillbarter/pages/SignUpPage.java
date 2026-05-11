package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Sign Up / Registration page.
 * Covers TC_015 – TC_020 (TS_003).
 */
public class SignUpPage {

    private final WebDriverWait wait;

    @FindBy(xpath = "//h1[normalize-space()='Create account']")
    private WebElement pageHeading;

    @FindBy(xpath = "//input[@type='text' and @placeholder='Your full name']")
    private WebElement nameField;

    @FindBy(xpath = "//input[@type='email']")
    private WebElement emailField;

    @FindBy(xpath = "//input[@type='password' and @placeholder='Min 6 characters']")
    private WebElement passwordField;

    @FindBy(xpath = "//input[@type='password' and @placeholder='Repeat password']")
    private WebElement confirmPasswordField;

    @FindBy(xpath = "//button[normalize-space()='Create Account']")
    private WebElement signUpBtn;

    @FindBy(xpath = "//div[contains(@class,'error-box')]")
    private WebElement errorMessage;

    @FindBy(xpath = "//p[contains(@class,'switch')]/a[@href='/login']")
    private WebElement backToSignInLink;

    @FindBy(xpath = "//div[contains(@class,'success')]")
    private WebElement successMessage;

    public SignUpPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void signUp(String name, String email, String password, String confirmPassword) {
        type(nameField, name);
        type(emailField, email);
        type(passwordField, password);
        type(confirmPasswordField, confirmPassword);
        clickSignUp();
    }

    public void clickSignUp() {
        wait.until(ExpectedConditions.elementToBeClickable(signUpBtn)).click();
    }

    public void clickBackToSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(backToSignInLink)).click();
    }

    public boolean isBackToSignInLinkPresent() {
        return isDisplayed(backToSignInLink);
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return textOf(errorMessage);
    }

    public String getSuccessMessage() {
        return textOf(successMessage);
    }

    public String getPageHeading() {
        return textOf(pageHeading);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void type(WebElement el, String text) {
        wait.until(ExpectedConditions.visibilityOf(el));
        el.clear();
        el.sendKeys(text);
    }

    private String textOf(WebElement el) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(el)).getText().trim();
        } catch (Exception e) {
            return "";
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
