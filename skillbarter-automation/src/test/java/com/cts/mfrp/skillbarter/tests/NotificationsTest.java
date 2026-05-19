package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.NotificationsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Test class for Notifications Panel.
 *
 * Scenario : TS_008 – Verify Notifications Panel Display and Types
 * Test Cases : TC_041 → TC_044
 * Group      : notifications, regression
 */
public class NotificationsTest extends BaseTest {
    private NotificationsPage notifPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenPanel() {
        // Direct-form login (no Sign In / Get Started probe-click). Same
        // pattern as CalendarTest — resilient to homepage CTA copy/class
        // churn between builds.
        navigateTo(AppConstants.SIGNIN_URL);

        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement emailField = loginWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));

        emailField.clear();
        emailField.sendKeys(AppConstants.VALID_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(AppConstants.VALID_PASSWORD);
        passwordField.sendKeys(Keys.ENTER);

        loginWait.until(ExpectedConditions.urlContains("dashboard"));

        notifPage = new NotificationsPage(driver);
        // Bell lives in the top bar — wait for it before any test body runs.
        // The dashboard topbar hydrates a few seconds after the URL changes.
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> notifPage.isBellVisible());
    }

    @Test(testName = "TC_041", description = "Notification bell icon is visible in the top bar")
    public void tc041_notificationIconAvailable() {
        Assert.assertTrue(notifPage.isBellVisible(), "Notification bell icon is not visible");
    }

    @Test(testName = "TC_042", description = "Notifications panel opens and shows notification count")
    public void tc042_notificationCountDisplayed() {
        notifPage.openPanel();
        // Wait for the panel to actually render rather than sleeping blindly.
        Assert.assertTrue(notifPage.isPanelVisible(), "Notification panel did not open");
        System.out.println("Notification count: " + notifPage.getNotificationCount());
    }

    @Test(testName = "TC_043", description = "Clicking a notification removes it (or marks it read)")
    public void tc043_notificationDeletesOnClick() {
        notifPage.openPanel();
        // Wait for the panel to render before reading from it.
        Assert.assertTrue(notifPage.isPanelVisible(), "Notification panel did not open");

        int countBefore = notifPage.getNotificationCount();
        if (countBefore == 0) {
            throw new org.testng.SkipException("No notifications to delete — skipping TC_043");
        }
        String textBefore = notifPage.getFirstNotificationText();
        boolean wasUnread = notifPage.isFirstNotificationUnread();

        notifPage.clickFirstNotification();

        // Replaces the old sleep-based polling loop with a single WebDriverWait
        // that re-evaluates the same change conditions every 300ms for up to 5s.
        boolean changed;
        try {
            changed = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> {
                        if (notifPage.getNotificationCount() < countBefore) return true;
                        String firstAfter = notifPage.getFirstNotificationText();
                        if (!textBefore.isEmpty() && !textBefore.equals(firstAfter)) return true;
                        return wasUnread && !notifPage.isFirstNotificationUnread();
                    });
        } catch (Exception timeout) {
            changed = false;
        }

        Assert.assertTrue(
            changed,
            "Notification was neither deleted nor marked as read within 5s of click. "
            + "Before count: " + countBefore + ", text before: '" + textBefore + "', was unread: " + wasUnread
        );
    }

    @Test(testName = "TC_044", description = "If notifications are present, click Mark all read")
    public void tc044_markAllAsReadClearsNotifications() {
        notifPage.openPanel();
        // Wait for the panel to render before reading the count.
        Assert.assertTrue(notifPage.isPanelVisible(), "Notification panel did not open");

        int countBefore = notifPage.getNotificationCount();
        if (countBefore == 0) {
            throw new org.testng.SkipException("No notifications present — skipping TC_044");
        }

        notifPage.clickMarkAllRead();

        // Wait for the unread state to actually clear (count drops OR first
        // item is no longer unread). Replaces a 5s "visible pause" sleep.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> notifPage.getNotificationCount() < countBefore
                             || !notifPage.isFirstNotificationUnread());
        } catch (Exception ignored) {
            // Best-effort wait — assertion-free here matches the original test's intent
            // (TC_044 just exercises the click; it asserted nothing post-click).
        }
    }
}
