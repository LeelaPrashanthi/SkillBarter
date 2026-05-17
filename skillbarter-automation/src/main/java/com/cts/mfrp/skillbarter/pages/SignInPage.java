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
 * Page Object for the Sign In page (auth-card / "Welcome back").
 *
 * Covers TC_008 – TC_014 (TS_002).
 *
 * Locator strategy: XPath everywhere. The page has no IDs or stable test
 * hooks — Angular's _ngcontent attributes change per build — so we anchor
 * on visible labels ("Email", "Password", "Sign In", "Forgot password?",
 * "Sign Up") and input @type. The Sign In button starts with the disabled
 * attribute and only becomes enabled once both fields satisfy validation
 * (email format + password minlength=6), so click waits use
 * elementToBeClickable, which respects the disabled state.
 */
public class SignInPage {

    private static final Logger log = LogManager.getLogger(SignInPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//div[contains(@class,'auth-card')]//div[contains(@class,'brand')]")
    private WebElement brand;

    @FindBy(xpath = "//div[contains(@class,'auth-card')]/h1")
    private WebElement pageHeading;

    @FindBy(xpath = "//div[contains(@class,'auth-card')]//p[contains(@class,'sub')]")
    private WebElement subHeading;

    @FindBy(xpath = "//div[contains(@class,'form-group')][./label[normalize-space()='Email']]" +
            "//input[@type='email']")
    private WebElement emailField;

    @FindBy(xpath = "//div[contains(@class,'form-group')][./label[normalize-space()='Password']]" +
            "//input[@type='password']")
    private WebElement passwordField;

    @FindBy(xpath = "//div[contains(@class,'forgot-wrap')]//a[normalize-space()='Forgot password?']")
    private WebElement forgotPasswordLink;

    @FindBy(xpath = "//div[contains(@class,'auth-card')]" +
            "//button[contains(@class,'btn-primary') and normalize-space()='Sign In']")
    private WebElement signInBtn;

    @FindBy(xpath = "//p[contains(@class,'switch')]/a[normalize-space()='Sign Up']")
    private WebElement signUpLink;

    // Error container isn't in the snapshot you pasted, but appears at runtime
    // after a failed submit. Match by common Angular class patterns + role=alert.
    @FindBy(xpath = "//div[contains(@class,'auth-card')]" +
            "//*[contains(@class,'error') or contains(@class,'error-box') " +
            "    or contains(@class,'alert') or @role='alert']")
    private WebElement errorMessage;

    public SignInPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Headings / brand ─────────────────────────────────────────────────────

    public String getBrand() {
        return safeText(brand);
    }

    public String getPageHeading() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageHeading)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String getSubHeading() {
        return safeText(subHeading);
    }

    // ── Form inputs ──────────────────────────────────────────────────────────

    public void enterEmail(String email) {
        clearAndType(emailField, email, "email");
    }

    public void enterPassword(String password) {
        clearAndType(passwordField, password, "password");
    }

    public void clickSignIn() {
        click(signInBtn, "Sign In button");
    }

    /** Full flow: type credentials and submit. */
    public void signIn(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    /** Sign In starts disabled until both fields are valid (email format + 6-char password). */
    public boolean isSignInButtonEnabled() {
        try {
            return signInBtn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Errors / links ───────────────────────────────────────────────────────

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

    public void clickSignUpLink() {
        click(signUpLink, "Sign Up link");
    }

    public boolean isForgotPasswordLinkPresent() {
        return isDisplayed(forgotPasswordLink);
    }

    public boolean isSignUpLinkPresent() {
        return isDisplayed(signUpLink);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void clearAndType(WebElement el, String text, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
            log.debug("Typed into {}", name);
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

    private String safeText(WebElement el) {
        try {
            return el.getText().trim();
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