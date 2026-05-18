package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class UserProfilePage {

    private static final Logger log = LogManager.getLogger(UserProfilePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "input[placeholder='Full name']")
    private WebElement nameField;

    @FindBy(css = "textarea[placeholder*='Tell others']")
    private WebElement descriptionField;

    @FindBy(xpath = "//div[contains(@class,'skills-col')][.//label[contains(text(),'Teach')]]//select")
    private WebElement skillsToTeachDropdown;

    @FindBy(xpath = "//div[contains(@class,'skills-col')][.//label[contains(text(),'Teach')]]//button[normalize-space()='Add']")
    private WebElement addSkillToTeachBtn;

    @FindBy(xpath = "//div[contains(@class,'skills-col')][.//label[contains(text(),'Learn')]]//select")
    private WebElement skillsToLearnDropdown;

    @FindBy(xpath = "//div[contains(@class,'skills-col')][.//label[contains(text(),'Learn')]]//button[normalize-space()='Add']")
    private WebElement addSkillToLearnBtn;

    @FindBy(xpath = "//div[contains(@class,'pactions')]//button[normalize-space()='Save Profile']")
    private WebElement saveProfileBtn;

    // Avatar flow: Change Photo → (sendKeys to file input) → Update Photo
    @FindBy(xpath = "//div[contains(@class,'pav-section')]//button[normalize-space()='Change Photo']")
    private WebElement changePhotoBtn;

    @FindBy(css = "input.file-input[type='file']")
    private WebElement avatarInput;

    @FindBy(xpath = "//div[contains(@class,'pav-section')]//button[normalize-space()='Upload Photo' or normalize-space()='Update Photo']")
    private WebElement uploadPhotoBtn;

    @FindBy(css = ".pav-section .pav img.pimg")
    private WebElement avatarImg;

    @FindBy(xpath = "//*[contains(@class,'success-box') or contains(@class,'photo-info') or contains(normalize-space(.),'successfully')]")
    private WebElement successMessage;

    @FindBy(css = ".error-box")
    private WebElement errorMessage;

    @FindBy(css = ".user-btn")
    private WebElement userMenuBtn;

    @FindBy(xpath = "//*[contains(@class,'dd-item') or contains(@class,'dropdown')]//*[normalize-space()='Profile']")
    private WebElement profileMenuItem;

    public UserProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Page state ────────────────────────────────────────────────────────────

    public boolean isPageLoaded() {
        try { return wait.until(ExpectedConditions.visibilityOf(nameField)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // ── Edit actions ──────────────────────────────────────────────────────────

    public void clearAndEnterName(String name) { clearAndType(nameField, name, "Name"); }

    public void clearName() {
        try {
            wait.until(ExpectedConditions.visibilityOf(nameField));
            nameField.click();
            nameField.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
            nameField.sendKeys(org.openqa.selenium.Keys.DELETE);
        } catch (Exception e) {
            log.warn("clearName failed: {}", e.getMessage());
        }
    }

    public void clearAndEnterDescription(String description) {
        clearAndType(descriptionField, description, "Description");
    }

    public void addSkillToTeach(String s) { selectSkill(skillsToTeachDropdown, addSkillToTeachBtn, s, "Skills to Teach"); }
    public void addSkillToLearn(String s) { selectSkill(skillsToLearnDropdown, addSkillToLearnBtn, s, "Skills to Learn"); }

    public void editProfile(String name, String description, String skillToTeach, String skillToLearn) {
        clearAndEnterName(name);
        clearAndEnterDescription(description);
        addSkillToTeach(skillToTeach);
        addSkillToLearn(skillToLearn);
    }

    public void clickSaveProfile() { click(saveProfileBtn, "Save Profile"); }

    public boolean isSaveButtonDisabled() {
        try {
            wait.until(ExpectedConditions.visibilityOf(saveProfileBtn));
            return !saveProfileBtn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Topbar dropdown ───────────────────────────────────────────────────────

    public void openUserDropdown() { click(userMenuBtn, "User menu (topbar)"); }

    public void clickProfileFromDropdown() {
        openUserDropdown();
        click(profileMenuItem, "Profile (dropdown)");
    }

    public boolean isProfilePageOpenedFromDropdown() {
        try { return wait.until(ExpectedConditions.urlContains("/profile")); }
        catch (Exception e) { return false; }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    public boolean isSuccessMessageDisplayed() {
        try { return wait.until(ExpectedConditions.visibilityOf(successMessage)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isErrorMessageDisplayed() {
        try { return wait.until(ExpectedConditions.visibilityOf(errorMessage)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public String getSuccessMessage() {
        try { return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getErrorMessage() {
        try { return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    // ── Avatar flow: Change Photo → sendKeys → Update Photo ───────────────────

    public void changeAvatar(String absolutePath) {
        clickChangePhoto();
        uploadAvatarFile(absolutePath);
        clickUploadPhoto();
    }

    public void clickChangePhoto() { click(changePhotoBtn, "Change Photo"); }

    public void uploadAvatarFile(String absolutePath) {
        try {
            avatarInput.sendKeys(absolutePath);
            log.debug("File path set on avatar input: {}", absolutePath);
        } catch (Exception e) { log.warn("uploadAvatarFile failed: {}", e.getMessage()); }
    }

    public void clickUploadPhoto() { click(uploadPhotoBtn, "Upload/Update Photo"); }

    public boolean isAvatarImageDisplayed() {
        try { return wait.until(ExpectedConditions.visibilityOf(avatarImg)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public String getAvatarSrc() {
        try { return avatarImg.getAttribute("src"); } catch (Exception e) { return ""; }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void clearAndType(WebElement el, String text, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
        } catch (Exception e) { log.warn("clearAndType failed on '{}': {}", name, e.getMessage()); }
    }

    private void selectSkill(WebElement dropdown, WebElement addBtn, String skillName, String which) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(dropdown));
            new Select(dropdown).selectByVisibleText(skillName);
            wait.until(ExpectedConditions.elementToBeClickable(addBtn)).click();
        } catch (Exception e) { log.warn("selectSkill failed on '{}' ({}): {}", which, skillName, e.getMessage()); }
    }

    private void click(WebElement el, String name) {
        try { wait.until(ExpectedConditions.elementToBeClickable(el)).click(); }
        catch (Exception e) { log.warn("Click failed on '{}': {}", name, e.getMessage()); }
    }
}