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
 * Page Object for the Saved Profiles page.
 * Covers TC_047 – TC_050 (TS_010).
 */
public class SavedProfilesPage {

    private static final Logger log = LogManager.getLogger(SavedProfilesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='page-title']")
    private WebElement pageTitle;

    @FindBy(css = "[class*='saved-card'], [class*='profile-card'], [class*='saved-profile-item']")
    private List<WebElement> profileCards;

    @FindBy(css = "[class*='view-profile'], a[class*='view-profile'], button[class*='view']")
    private List<WebElement> viewProfileBtns;

    @FindBy(css = "button[class*='message'], a[class*='message-btn']")
    private List<WebElement> messageBtns;

    @FindBy(css = "button[class*='remove'], [class*='unsave'], [class*='remove-saved']")
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

    public boolean hasProfiles() { return !profileCards.isEmpty(); }

    public int getProfileCount() { return profileCards.size(); }

    public String getFirstProfileText() {
        try { return profileCards.get(0).getText(); } catch (Exception e) { return ""; }
    }

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
