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
 * Page Object for the Progress page.
 * Covers TC_069 – TC_073 (TS_015).
 */
public class ProgressPage {

    private static final Logger log = LogManager.getLogger(ProgressPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='page-title']")
    private WebElement pageTitle;

    @FindBy(css = "[class*='level'], [class*='xp-level'], [class*='user-level']")
    private WebElement levelDisplay;

    @FindBy(css = "[class*='xp'], [class*='experience-points']")
    private WebElement xpDisplay;

    @FindBy(css = "[class*='hours-learning'], [class*='learning-hours'], " +
            "[class*='activity-stat']:first-of-type")
    private WebElement hoursLearning;

    @FindBy(css = "[class*='hours-teaching'], [class*='teaching-hours'], " +
            "[class*='activity-stat']:last-of-type")
    private WebElement hoursTeaching;

    @FindBy(css = "[class*='badge'], [class*='achievement']")
    private List<WebElement> badges;

    @FindBy(css = "[class*='locked-badge'], [class*='badge-locked']")
    private List<WebElement> lockedBadges;

    @FindBy(css = "[class*='next-goal'], [class*='goals-section']")
    private WebElement nextGoalsSection;

    @FindBy(css = "button[class*='schedule'], a[class*='schedule']")
    private WebElement scheduleSessionBtn;

    public ProgressPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isLevelDisplayed() { return isDisplayed(levelDisplay); }
    public boolean isXpDisplayed()    { return isDisplayed(xpDisplay); }

    public String getLevelText() {
        try { return levelDisplay.getText().trim(); } catch (Exception e) { return ""; }
    }

    public boolean isHoursLearningDisplayed() { return isDisplayed(hoursLearning); }
    public boolean isHoursTeachingDisplayed() { return isDisplayed(hoursTeaching); }

    public int getBadgeCount() { return badges.size(); }
    public int getLockedBadgeCount() { return lockedBadges.size(); }

    public boolean isBadgeSectionVisible() {
        return !badges.isEmpty() && badges.get(0).isDisplayed();
    }

    public boolean isNextGoalsSectionVisible() { return isDisplayed(nextGoalsSection); }

    public String getNextGoalsText() {
        try { return nextGoalsSection.getText().trim(); } catch (Exception e) { return ""; }
    }

    public void clickScheduleSession() {
        click(scheduleSessionBtn, "Schedule a Session");
    }

    private void click(WebElement el, String name) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
            log.debug("Clicked: {}", name);
        } catch (Exception e) {
            log.warn("Click failed on '{}': {}", name, e.getMessage());
        }
    }

    private boolean isDisplayed(WebElement el) {
        try { return el.isDisplayed(); } catch (Exception e) { return false; }
    }
}
