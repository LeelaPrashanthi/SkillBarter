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
 * Page Object for the Notifications Panel component.
 * Covers TC_041 – TC_042 (TS_008).
 */
public class NotificationsPage {

    private static final Logger log = LogManager.getLogger(NotificationsPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "[class*='notif-panel'], [class*='notifications-panel'], " +
            "[class*='notification-dropdown']")
    private WebElement notifPanel;

    @FindBy(css = "[class*='notif-header'], [class*='panel-header']")
    private WebElement notifHeader;

    @FindBy(css = "[class*='notif-item'], [class*='notification-entry'], " +
            "[class*='notif-card']")
    private List<WebElement> notifItems;

    @FindBy(css = "[class*='close-notif'], [class*='back-btn'], [class*='panel-close']")
    private WebElement closeBtn;

    public NotificationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(notifPanel)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public String getHeaderText() {
        try { return notifHeader.getText().trim(); } catch (Exception e) { return ""; }
    }

    public int getNotificationCount() { return notifItems.size(); }

    public boolean hasNotifications() { return !notifItems.isEmpty(); }

    public String getFirstNotificationText() {
        try { return notifItems.get(0).getText().trim(); } catch (Exception e) { return ""; }
    }

    public void closePanel() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(closeBtn)).click();
        } catch (Exception e) {
            log.warn("closePanel failed: {}", e.getMessage());
        }
    }
}
