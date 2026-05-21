package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Profile Creation page (TC_021 – TC_024).
 */
public class ProfileCreationPage {

    private final WebDriverWait wait;

    @FindBy(xpath = "//div/label[contains(text(),'Name')]/following::input[1]")
    private WebElement nameField;

    @FindBy(xpath = "//div/label[text()='Profile Description']/following::textarea")
    private WebElement descriptionField;

    @FindBy(xpath = "//label[text()='Skills I Have (Teach)']/parent::div/div[2]/select")
    private WebElement skillsToTeachDropdown;

    @FindBy(xpath = "//label[text()='Skills I Need to Learn']/parent::div/div[2]/select")
    private WebElement skillsToLearnDropdown;

    @FindBy(xpath = "//button[text()='Save Profile']")
    private WebElement saveProfileBtn;

    // Reset / Cancel: tolerant locators because the live build varies between
    // <button> and <a>, and the label sometimes carries extra whitespace.
    @FindBy(xpath = "//button[normalize-space()='Reset'] | //a[normalize-space()='Reset']")
    private WebElement resetBtn;

    @FindBy(xpath = "//button[normalize-space()='Cancel'] | //a[normalize-space()='Cancel']")
    private WebElement cancelBtn;

    @FindBy(xpath = "//*[contains(@class,'error') or @role='alert']")
    private WebElement errorMessage;

    @FindBy(xpath = "//*[contains(@class,'success')]")
    private WebElement successMessage;

    public ProfileCreationPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void enterName(String name) {
        clearAndType(nameField, name);
    }

    public void enterDescription(String description) {
        clearAndType(descriptionField, description);
    }

    public void clickSkillsToTeachDropdown() {
        click(skillsToTeachDropdown);
    }

    public void clickSkillsToLearnDropdown() {
        click(skillsToLearnDropdown);
    }

    public void clickContinue() {
        click(saveProfileBtn);
    }

    public void clickReset() {
        click(resetBtn);
    }

    public void clickCancel() {
        click(cancelBtn);
    }

    /**
     * True if a Reset button/link is actually rendered. Distinguishes a real
     * "click did nothing" bug from "selector matched nothing" in test logs.
     */
    public boolean isResetButtonPresent() {
        try { return resetBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** Same as {@link #isResetButtonPresent} but for Cancel. */
    public boolean isCancelButtonPresent() {
        try { return cancelBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** Returns the current value of the Name input — used to verify Reset cleared it. */
    public String getNameValue() {
        try { return nameField.getAttribute("value"); } catch (Exception e) { return ""; }
    }

    /** Returns the current value of the Description textarea — used to verify Reset cleared it. */
    public String getDescriptionValue() {
        try { return descriptionField.getAttribute("value"); } catch (Exception e) { return ""; }
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

    // ── private helpers ───────────────────────────────────────────────────────

    private void clearAndType(WebElement el, String text) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
        } catch (Exception ignored) {}
    }

    private void click(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (Exception ignored) {}
    }
}
