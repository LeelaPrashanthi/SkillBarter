package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProgressPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test class for Progress page.
 *
 * Scenario   : TS_015 – Verify Progress Page Tracking and Display
 * Test Cases : TC_069 → TC_078
 * Group      : progress, regression
 */
public class ProgressTest extends BaseTest {

    private ProgressPage progressPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenProgress() {
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

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'progress')]"))).click();

        wait.until(ExpectedConditions.urlContains("progress"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Level')] | //*[contains(text(),'XP')]")));

        progressPage = new ProgressPage(driver);
    }

    @Test(testName = "TC_069", description = "Print XP")
    public void tc069_xpDisplayed() {
        System.out.println("XP: " + progressPage.getXpText());
        pause();
    }

    @Test(testName = "TC_070", description = "Print Your Level")
    public void tc070_yourLevelDisplayed() {
        System.out.println("Your Level: " + progressPage.getLevelText());
        pause();
    }

    @Test(testName = "TC_071", description = "Print Activity")
    public void tc071_activitiesDisplayed() {
        System.out.println("Activity: " + progressPage.getActivityText());
        pause();
    }

    @Test(testName = "TC_072", description = "Print Total Sessions")
    public void tc072_totalSessionsDisplayed() {
        System.out.println("Total Sessions: " + progressPage.getTotalSessionsText());
        pause();
    }

    @Test(testName = "TC_073", description = "Print Completed")
    public void tc073_completedDisplayed() {
        System.out.println("Completed: " + progressPage.getCompletedText());
        pause();
    }

    @Test(testName = "TC_074", description = "Print Completion Rate")
    public void tc074_completionRateDisplayed() {
        System.out.println("Completion Rate: " + progressPage.getCompletionRateText());
        pause();
    }

    @Test(testName = "TC_075", description = "Print Ratings")
    public void tc075_ratingsDisplayed() {
        System.out.println("Ratings: " + progressPage.getRatingsText());
        pause();
    }

    @Test(testName = "TC_076", description = "Print Reviews Received")
    public void tc076_reviewsReceivedDisplayed() {
        System.out.println("Reviews Received: " + progressPage.getReviewsReceivedText());
        pause();
    }

    @Test(testName = "TC_077", description = "Print badges count, each badge text and status")
    public void tc077_badgesDisplayed() {
        List<String> badges = progressPage.getBadgeTexts();
        if (badges.isEmpty()) {
            System.out.println("Badges: no badges");
            pause();
            return;
        }
        int enabled = 0;
        for (String b : badges) if (progressPage.isBadgeEnabled(b)) enabled++;

        System.out.println("Badges total: " + badges.size() + ", enabled: " + enabled
                + ", locked: " + (badges.size() - enabled));
        for (int i = 0; i < badges.size(); i++) {
            String status = progressPage.isBadgeEnabled(badges.get(i)) ? "[Enabled]" : "[Locked]";
            System.out.println("  [" + (i + 1) + "] " + status + " " + badges.get(i));
        }
        pause();
    }

    @Test(testName = "TC_078", description = "Print Next Goals")
    public void tc078_nextGoalsDisplayed() {
        System.out.println("Next Goals: " + progressPage.getNextGoalsText());
        pause();
    }

    /** 2-second pause so the element stays visible on screen. */
    private void pause() {
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
