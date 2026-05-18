package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the post-signup Profile Setup page (/profile-setup).
 * Reached automatically after a successful Sign Up — collects the user's
 * display name and "about yourself" bio before navigating to the dashboard.
 */
public class ProfileSetupPage {

    private final WebDriverWait wait;
    private final WebDriver driver;

    @FindBy(xpath = "//input[@type='text' and (contains(@placeholder,'name') or contains(@placeholder,'Name'))]")
    private WebElement nameField;

    @FindBy(xpath = "//textarea[contains(@placeholder,'about') or contains(@placeholder,'About') " +
            "or contains(@placeholder,'yourself') or contains(@placeholder,'bio')]")
    private WebElement aboutField;

    @FindBy(xpath = "//button[contains(normalize-space(),'Continue to Dashboard') " +
            "or contains(normalize-space(),'Continue')]")
    private WebElement continueToDashboardBtn;

    public ProfileSetupPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("profile-setup"));
        wait.until(ExpectedConditions.visibilityOf(nameField));
    }

    public void enterName(String name) {
        wait.until(ExpectedConditions.visibilityOf(nameField));
        nameField.clear();
        nameField.sendKeys(name);
    }

    public void enterAbout(String about) {
        wait.until(ExpectedConditions.visibilityOf(aboutField));
        aboutField.clear();
        aboutField.sendKeys(about);
    }

    public void clickContinueToDashboard() {
        wait.until(ExpectedConditions.elementToBeClickable(continueToDashboardBtn)).click();
    }

    public void completeProfileSetup(String name, String about) {
        waitUntilLoaded();
        enterName(name);
        enterAbout(about);
        clickContinueToDashboard();
        wait.until(ExpectedConditions.urlContains("dashboard"));
    }
}
