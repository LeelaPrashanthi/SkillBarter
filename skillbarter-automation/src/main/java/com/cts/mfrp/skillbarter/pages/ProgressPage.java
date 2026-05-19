package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the Progress page (TC_069 – TC_078).
 * Each method returns the section's text, or "no <thing>" if missing.
 */
public class ProgressPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ProgressPage(WebDriver driver) {
        this.driver = driver;
        // Bumped from 15s → 30s. Individual sections on Progress can hydrate
        // a few seconds after the page-shell waits in the test's BeforeMethod
        // fire — a 15s budget per section was timing out on slow runs.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }

    public String getXpText() {
        return readSection("//*[contains(text(),'XP')]/..", "no xp");
    }

    public String getLevelText() {
        return readSection("//*[contains(text(),'Your level')]/..", "no level");
    }

    public String getActivityText() {
        return readSection("//*[contains(text(),'Activity')]/following::div", "no activity");
    }

    public String getTotalSessionsText() {
        return readSection("//*[contains(text(),'Total sessions')]/..", "no sessions");
    }

    public String getCompletedText() {
        // Was anchored on a brittle absolute XPath; switched to text-anchored
        // lookup like the other sections so it survives DOM restructuring.
        return readSection("//*[contains(text(),'Completed')]/..", "no completed sessions");
    }

    public String getCompletionRateText() {
        return readSection("//*[contains(text(),'Completion rate')]/..", "no completion rate");
    }

    public String getRatingsText() {
        return readSection("//*[contains(text(),'Ratings')]/..", "no ratings");
    }

    public String getReviewsReceivedText() {
        return readSection("//*[contains(text(),'Reviews Received')]/..", "no reviews received");
    }

    public String getNextGoalsText() {
        return readSection("//*[contains(text(),'Next Goals')]/..", "no next goals");
    }

    /** Returns each badge's text. Use {@link #isBadgeEnabled(String)} to classify enabled/locked. */
    public List<String> getBadgeTexts() {
        List<String> result = new ArrayList<>();
        List<WebElement> badges = driver.findElements(By.xpath(
                "//div[contains(@class,'badge') "
                + "and not(contains(@class,'sp-badge')) "
                + "and not(contains(@class,'badges')) "
                + "and not(contains(@class,'section')) "
                + "and not(contains(@class,'panel'))]"));
        for (WebElement b : badges) {
            try {
                result.add(b.getText().trim().replaceAll("\\s+", " "));
            } catch (Exception e) {
                result.add("(stale)");
            }
        }
        return result;
    }

    /** A badge is enabled if its text contains "Unlocked" (case-insensitive). */
    public boolean isBadgeEnabled(String badgeText) {
        return badgeText != null && badgeText.toLowerCase().contains("unlocked");
    }

    // ── private helper ────────────────────────────────────────────────────────

    /** Finds the first visible element matching xpath and returns its text; fallback if missing. */
    private String readSection(String xpath, String missingMsg) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            String text = el.getText().trim().replaceAll("\\s+", " ");
            return text.isEmpty() ? missingMsg : text;
        } catch (Exception e) {
            return missingMsg;
        }
    }
}
