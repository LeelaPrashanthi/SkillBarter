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
 * Page Object for the Profile Creation page.
 * Covers TC_021 – TC_025 (TS_004).
 */
public class ProfileCreationPage {

    private static final Logger log = LogManager.getLogger(ProfileCreationPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='profile-title'], [class*='heading']")
    private WebElement pageHeading;

    @FindBy(css = "input[name='name'], input[id='name'], input[placeholder*='name' i]")
    private WebElement nameField;

    @FindBy(css = "textarea[name='description'], textarea[id='description'], " +
            "textarea[placeholder*='description' i], textarea[placeholder*='bio' i]")
    private WebElement descriptionField;

    @FindBy(css = "[class*='skills-teach'], [id*='teach'], " +
            "[placeholder*='teach' i], [class*='skillsToTeach']")
    private WebElement skillsToTeachDropdown;

    @FindBy(css = "[class*='skills-learn'], [id*='learn'], " +
            "[placeholder*='learn' i], [class*='skillsToLearn']")
    private WebElement skillsToLearnDropdown;

    @FindBy(css = "[class*='language'], [id*='language'], [placeholder*='language' i]")
    private WebElement languagesDropdown;

    @FindBy(css = "button[type='submit'], button[class*='continue'], button[class*='save-profile']")
    private WebElement continueBtn;

    @FindBy(css = "[class*='error'], .alert-danger, [role='alert']")
    private WebElement errorMessage;

    @FindBy(css = "[class*='success'], .alert-success")
    private WebElement successMessage;

    @FindBy(css = "a[href*='signin'], button[class*='back']")
    private WebElement backToSignInBtn;

    // Avatar / photo upload
    @FindBy(css = "input[type='file'], [class*='avatar-upload'], [class*='photo-upload']")
    private WebElement avatarInput;

    @FindBy(css = "[class*='avatar-preview'], [class*='photo-preview'], img.avatar")
    private WebElement avatarPreview;

    @FindBy(css = "[class*='click-to-change'], [class*='upload-btn'], label[for*='avatar']")
    private WebElement clickToChangeBtn;

    public ProfileCreationPage(WebDriver driver) {
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

    public void enterName(String name) {
        clearAndType(nameField, name, "Name");
    }

    public void enterDescription(String description) {
        clearAndType(descriptionField, description, "Description");
    }

    public void clickContinue() {
        click(continueBtn, "Continue button");
    }

    public void clickBackToSignIn() {
        click(backToSignInBtn, "Back to Sign In");
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

    public String getSuccessMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void uploadAvatar(String absoluteFilePath) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.cssSelector("input[type='file']")));
            avatarInput.sendKeys(absoluteFilePath);
            log.info("Avatar uploaded: {}", absoluteFilePath);
        } catch (Exception e) {
            log.warn("Avatar upload failed: {}", e.getMessage());
        }
    }

    public boolean isAvatarPreviewUpdated() {
        try {
            String src = wait.until(ExpectedConditions.visibilityOf(avatarPreview))
                    .getAttribute("src");
            return src != null && !src.contains("default");
        } catch (Exception e) {
            return false;
        }
    }

    public void clickSkillsToTeachDropdown() {
        click(skillsToTeachDropdown, "Skills to Teach dropdown");
    }

    public void clickSkillsToLearnDropdown() {
        click(skillsToLearnDropdown, "Skills to Learn dropdown");
    }

    public void clickLanguagesDropdown() {
        click(languagesDropdown, "Languages dropdown");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void clearAndType(WebElement el, String text, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
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
}
