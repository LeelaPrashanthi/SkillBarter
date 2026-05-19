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
import java.util.List;

/**
 * Page Object for the Saved Profiles page (/app/saved-profiles).
 * Covers TC_047 – TC_050 (TS_010).
 *
 * Locators are anchored on visible text labels ("Saved Profiles",
 * "View Profile", "Message", "% match") to stay robust against
 * React class-name churn between builds.
 */
public class SavedProfilesPage {

    private static final Logger log = LogManager.getLogger(SavedProfilesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//app-saved-profiles//h1[normalize-space()='Saved Profiles'] | //h1[normalize-space()='Saved Profiles']")
    private WebElement pageTitle;

    @FindBy(xpath = "//div[contains(@class,'mcard')]//a[normalize-space()='View Profile'] | //div[contains(@class,'mcard')]//button[normalize-space()='View Profile']")
    private List<WebElement> viewProfileBtns;

    @FindBy(xpath = "//div[contains(@class,'mcard')]//a[normalize-space()='Message'] | //div[contains(@class,'mcard')]//button[normalize-space()='Message']")
    private List<WebElement> messageBtns;

    /**
     * Remove/unsave control isn't visible in the current build. Locator is
     * intentionally permissive so it will start matching if the UI later
     * exposes a Remove button, X icon, or aria-labelled control.
     */
    @FindBy(xpath =
            "//button[contains(translate(., 'REMOVUNSAV', 'removunsav'), 'remove') " +
            "or contains(translate(., 'REMOVUNSAV', 'removunsav'), 'unsave')] " +
            "| //button[@aria-label and (" +
            "contains(translate(@aria-label, 'REMOVUNSAV', 'removunsav'), 'remove') " +
            "or contains(translate(@aria-label, 'REMOVUNSAV', 'removunsav'), 'unsave'))]")
    private List<WebElement> removeBtns;

    public SavedProfilesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isPageLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean hasProfiles() { return !viewProfileBtns.isEmpty(); }

    public int getProfileCount() { return viewProfileBtns.size(); }

    /**
     * Polls up to {@code timeoutSeconds} for at least one profile card to render.
     * Returns true as soon as one is found, false if none appear within the timeout.
     * Used by tests to distinguish "page rendered slowly" from "account is genuinely empty".
     */
    public boolean waitForProfilesToLoad(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(250))
                    .until(d -> !viewProfileBtns.isEmpty());
            return true;
        } catch (Exception e) {
            log.info("No saved profile cards rendered after {}s wait", timeoutSeconds);
            return false;
        }
    }

    public boolean hasRemoveControls() { return !removeBtns.isEmpty(); }

    public int getRemoveCount() { return removeBtns.size(); }

    public void clickViewProfileAt(int index) {
        if (index < viewProfileBtns.size()) click(viewProfileBtns.get(index), "View Profile");
    }

    public void clickMessageAt(int index) {
        if (index < messageBtns.size()) click(messageBtns.get(index), "Message");
    }

    public void clickRemoveAt(int index) {
        if (index < removeBtns.size()) click(removeBtns.get(index), "Remove saved profile");
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
