package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Notifications Panel (TC_041 – TC_044).
 */
public class NotificationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    /** Bell icon — second .icon-btn in the topbar (first is the theme toggle). */
    private static final By BELL_ICON = By.xpath(
            "(//div[contains(@class,'topbar-right')]//button[contains(@class,'icon-btn')])[2]");

    private static final By NOTIF_PANEL = By.xpath("//div[contains(@class,'notif-panel')]");
    private static final By NOTIF_ITEMS = By.xpath("//div[contains(@class,'notif-item')]");
    private static final By MARK_ALL_BTN = By.xpath("//button[text()='Mark all read']");

    public NotificationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    /** Clicks the notification bell to open the panel. */
    public void openPanel() {
        wait.until(ExpectedConditions.elementToBeClickable(BELL_ICON)).click();
    }

    public boolean isBellVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(BELL_ICON)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(NOTIF_PANEL)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getNotificationCount() {
        return driver.findElements(NOTIF_ITEMS).size();
    }

    public String getFirstNotificationText() {
        List<WebElement> items = driver.findElements(NOTIF_ITEMS);
        if (items.isEmpty()) return "";
        try {
            String t = items.get(0).getText();
            return t == null ? "" : t.trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** True if the first notification still carries the 'unread' class. */
    public boolean isFirstNotificationUnread() {
        List<WebElement> items = driver.findElements(NOTIF_ITEMS);
        if (items.isEmpty()) return false;
        try {
            String cls = items.get(0).getAttribute("class");
            return cls != null && cls.contains("unread");
        } catch (Exception e) {
            return false;
        }
    }

    /** Clicks the first notification using a real mouse Actions click. */
    public void clickFirstNotification() {
        List<WebElement> items = driver.findElements(NOTIF_ITEMS);
        if (items.isEmpty()) return;
        clickWithActions(items.get(0));
    }

    /** Clicks the Mark all read button. */
    public void clickMarkAllRead() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(MARK_ALL_BTN));
        clickWithActions(btn);
    }

    /** Scrolls into view + Actions click → JS click fallback. */
    private void clickWithActions(WebElement el) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            new Actions(driver).moveToElement(el).pause(150).click().perform();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", el);
        }
    }
}
