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
 * Page Object for the User Profile page (edit & public view).
 * Covers TC_043 – TC_046 (TS_009).
 */
public class UserProfilePage {

    private static final Logger log = LogManager.getLogger(UserProfilePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "input[name='name'], input[id='name']")
    private WebElement nameField;

    @FindBy(css = "textarea[name='description'], textarea[id='description']")
    private WebElement descriptionField;

    @FindBy(css = "button[class*='save-profile'], button[type='submit']")
    private WebElement saveProfileBtn;

    @FindBy(css = "button[class*='view-public'], a[class*='public-profile']")
    private WebElement viewPublicProfileBtn;

    @FindBy(css = "[class*='public-profile-view'], [class*='readonly-profile']")
    private WebElement publicProfileView;

    @FindBy(css = "button[class*='edit'], input[class*='edit']")
    private WebElement editBtn;

    @FindBy(css = "[class*='error'], [role='alert']")
    private WebElement errorMessage;

    @FindBy(css = "[class*='success'], .alert-success")
    private WebElement successMessage;

    @FindBy(css = "input[type='file'], [class*='change-avatar'], [class*='avatar-upload']")
    private WebElement avatarInput;

    @FindBy(css = "img[class*='avatar'], img[class*='profile-pic']")
    private WebElement avatarImg;

    public UserProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void clearAndEnterName(String name) {
        clearAndType(nameField, name, "Name");
    }

    public void clearAndEnterDescription(String description) {
        clearAndType(descriptionField, description, "Description");
    }

    public void clickSaveProfile() {
        click(saveProfileBtn, "Save Profile");
    }

    public void clickViewPublicProfile() {
        click(viewPublicProfileBtn, "View My Public Profile");
    }

    public boolean isPublicProfileViewVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(publicProfileView)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isEditButtonAbsent() {
        try {
            return !editBtn.isDisplayed();
        } catch (Exception e) { return true; }
    }

    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public String getSuccessMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public void uploadAvatar(String path) {
        try {
            avatarInput.sendKeys(path);
        } catch (Exception e) {
            log.warn("uploadAvatar failed: {}", e.getMessage());
        }
    }

    public String getAvatarSrc() {
        try { return avatarImg.getAttribute("src"); } catch (Exception e) { return ""; }
    }

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
        } catch (Exception e) {
            log.warn("Click failed on '{}': {}", name, e.getMessage());
        }
    }
}
