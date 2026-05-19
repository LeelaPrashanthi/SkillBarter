package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.NotificationsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

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
        navigateTo(AppConstants.BASE_URL);

        List<WebElement> entryBtns = driver.findElements(By.xpath(
                "//a[contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'sign in') "
                + "or contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'get started')] "
                + "| //button[contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'sign in') "
                + "or contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'get started')]"));
        for (WebElement btn : entryBtns) {
            try { if (btn.isDisplayed()) { btn.click(); break; } } catch (Exception ignored) {}
        }

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email']")));
        emailField.sendKeys(AppConstants.VALID_EMAIL);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='password' or @name='password']")));
        passwordField.sendKeys(AppConstants.VALID_PASSWORD);

        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (Exception ignored) {
            passwordField.sendKeys(Keys.RETURN);
        }

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("signin")));
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        notifPage = new NotificationsPage(driver);
    }

    @Test(testName = "TC_041", description = "Notification bell icon is visible in the top bar")
    public void tc041_notificationIconAvailable() {
        Assert.assertTrue(notifPage.isBellVisible(), "Notification bell icon is not visible");
    }

    @Test(testName = "TC_042", description = "Notifications panel opens and shows notification count")
    public void tc042_notificationCountDisplayed() {
        notifPage.openPanel();
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Assert.assertTrue(notifPage.isPanelVisible(), "Notification panel did not open");
        System.out.println("Notification count: " + notifPage.getNotificationCount());
    }

    @Test(testName = "TC_043", description = "Clicking a notification removes it (or marks it read)")
    public void tc043_notificationDeletesOnClick() {
        notifPage.openPanel();
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        int countBefore = notifPage.getNotificationCount();
        if (countBefore == 0) {
            throw new org.testng.SkipException("No notifications to delete — skipping TC_043");
        }
        String textBefore = notifPage.getFirstNotificationText();
        boolean wasUnread = notifPage.isFirstNotificationUnread();

        notifPage.clickFirstNotification();

        // Poll for up to 5s for any change: count drop, first-text change, or unread class removed.
        boolean changed = false;
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }

            int countAfter = notifPage.getNotificationCount();
            if (countAfter < countBefore) { changed = true; break; }

            String firstAfter = notifPage.getFirstNotificationText();
            if (!textBefore.isEmpty() && !textBefore.equals(firstAfter)) { changed = true; break; }

            if (wasUnread && !notifPage.isFirstNotificationUnread()) { changed = true; break; }
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
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        if (notifPage.getNotificationCount() == 0) {
            throw new org.testng.SkipException("No notifications present — skipping TC_044");
        }

        notifPage.clickMarkAllRead();

        // Visible pause so the cleared state stays on screen before the test ends.
        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
