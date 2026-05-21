package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProgressPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
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

        // Click the sidebar Progress link — its href comes from the live page,
        // so we don't have to guess whether the route is /progress or /app/progress.
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        pageWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'progress')]"))).click();

        pageWait.until(ExpectedConditions.urlContains("progress"));
        pageWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Level')] | //*[contains(text(),'XP')] "
                       + "| //*[contains(text(),'Activity')]")));

        progressPage = new ProgressPage(driver);
    }

    @Test(testName = "TC_069", description = "Print XP",
          groups = {"progress", "smoke", "regression"})
    public void tc069_xpDisplayed() {
        System.out.println("XP: " + progressPage.getXpText());
    }

    @Test(testName = "TC_070", description = "Print Your Level")
    public void tc070_yourLevelDisplayed() {
        System.out.println("Your Level: " + progressPage.getLevelText());
    }

    @Test(testName = "TC_071", description = "Print Activity")
    public void tc071_activitiesDisplayed() {
        System.out.println("Activity: " + progressPage.getActivityText());
    }

    @Test(testName = "TC_072", description = "Print Total Sessions")
    public void tc072_totalSessionsDisplayed() {
        System.out.println("Total Sessions: " + progressPage.getTotalSessionsText());
    }

    @Test(testName = "TC_073", description = "Print Completed")
    public void tc073_completedDisplayed() {
        System.out.println("Completed: " + progressPage.getCompletedText());
    }

    @Test(testName = "TC_074", description = "Print Completion Rate")
    public void tc074_completionRateDisplayed() {
        System.out.println("Completion Rate: " + progressPage.getCompletionRateText());
    }

    @Test(testName = "TC_075", description = "Print Ratings")
    public void tc075_ratingsDisplayed() {
        System.out.println("Ratings: " + progressPage.getRatingsText());
    }

    @Test(testName = "TC_076", description = "Print Reviews Received")
    public void tc076_reviewsReceivedDisplayed() {
        System.out.println("Reviews Received: " + progressPage.getReviewsReceivedText());
    }

    @Test(testName = "TC_077", description = "Print badges count, each badge text and status")
    public void tc077_badgesDisplayed() {
        List<String> badges = progressPage.getBadgeTexts();
        if (badges.isEmpty()) {
            System.out.println("Badges: no badges");
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
    }

    @Test(testName = "TC_078", description = "Print Next Goals")
    public void tc078_nextGoalsDisplayed() {
        System.out.println("Next Goals: " + progressPage.getNextGoalsText());
    }

}
