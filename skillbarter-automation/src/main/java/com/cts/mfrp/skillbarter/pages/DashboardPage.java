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
 * Page Object for the User Dashboard.
 * Covers TC_026 – TC_031 (TS_005) and sidebar navigation (TC_032–TC_034, TS_006).
 */
public class DashboardPage {

    private static final Logger log = LogManager.getLogger(DashboardPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Sidebar Navigation ────────────────────────────────────────────────────
    @FindBy(css = "[class*='sidebar'] a[href*='dashboard'], nav a[class*='dashboard']")
    private WebElement sidebarDashboard;

    @FindBy(css = "[class*='sidebar'] a[href*='matches'], nav a[class*='matches']")
    private WebElement sidebarMatches;

    @FindBy(css = "[class*='sidebar'] a[href*='chat'], [class*='sidebar'] a[href*='messenger']")
    private WebElement sidebarChat;

    @FindBy(css = "[class*='sidebar'] a[href*='calendar']")
    private WebElement sidebarCalendar;

    @FindBy(css = "[class*='sidebar'] a[href*='progress']")
    private WebElement sidebarProgress;

    @FindBy(css = "[class*='sidebar'] a[href*='community']")
    private WebElement sidebarCommunity;

    // ── Top Nav Bar ───────────────────────────────────────────────────────────
    @FindBy(css = "[class*='sp-counter'], [class*='skill-points'], [class*='sp-balance']")
    private WebElement spCounter;

    @FindBy(css = "[class*='sp-add'], [class*='earn-sp'], button[class*='plus']")
    private WebElement spPlusIcon;

    @FindBy(css = "[class*='notification-bell'], button[class*='bell'], [class*='notif-btn']")
    private WebElement notificationBell;

    @FindBy(css = "[class*='user-profile'], [class*='profile-pic'], [class*='avatar-btn']")
    private WebElement userProfileNav;

    // ── Activity Section ──────────────────────────────────────────────────────
    @FindBy(css = "button[class*='schedule'], a[class*='schedule'], " +
            "[class*='activity'] button, [class*='session-cta']")
    private WebElement scheduleSessionBtn;

    // ── Matches Section ───────────────────────────────────────────────────────
    @FindBy(css = "[class*='matches-section'], [class*='suggested-matches'], " +
            "[class*='match-list']")
    private WebElement matchesSection;

    @FindBy(css = "[class*='match-card'], [class*='profile-card']")
    private List<WebElement> matchCards;

    @FindBy(css = "button[class*='match'], button[class*='connect']")
    private List<WebElement> matchButtons;

    // ── Calendar Section ──────────────────────────────────────────────────────
    @FindBy(css = "[class*='calendar-section'], [class*='mini-calendar'], " +
            "[class*='calendar-widget']")
    private WebElement calendarSection;

    @FindBy(css = "[class*='calendar'] td, [class*='day-cell']")
    private List<WebElement> calendarDays;

    @FindBy(css = "[class*='empty-state'], [class*='no-session'], [class*='no-lessons']")
    private WebElement emptyStateMessage;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    public boolean isSidebarVisible() {
        return isDisplayed(sidebarDashboard);
    }

    public boolean areAllSidebarItemsPresent() {
        return isDisplayed(sidebarDashboard)
                && isDisplayed(sidebarMatches)
                && isDisplayed(sidebarCalendar)
                && isDisplayed(sidebarProgress)
                && isDisplayed(sidebarCommunity);
    }

    public void clickSidebarMatches() {
        click(sidebarMatches, "Sidebar Matches");
    }

    public void clickSidebarChat() {
        click(sidebarChat, "Sidebar Chat");
    }

    public void clickSidebarCalendar() {
        click(sidebarCalendar, "Sidebar Calendar");
    }

    public void clickSidebarProgress() {
        click(sidebarProgress, "Sidebar Progress");
    }

    public void clickSidebarCommunity() {
        click(sidebarCommunity, "Sidebar Community");
    }

    // ── SP Counter ────────────────────────────────────────────────────────────

    public boolean isSpCounterDisplayed() {
        return isDisplayed(spCounter);
    }

    public String getSpCounterText() {
        try {
            return spCounter.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void clickSpPlusIcon() {
        click(spPlusIcon, "SP + icon");
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    public void clickNotificationBell() {
        click(notificationBell, "Notification bell");
    }

    public boolean isNotificationBellDisplayed() {
        return isDisplayed(notificationBell);
    }

    // ── User Profile Nav ──────────────────────────────────────────────────────

    public void clickUserProfileNav() {
        click(userProfileNav, "User Profile nav");
    }

    // ── Activity / Schedule ───────────────────────────────────────────────────

    public void clickScheduleSession() {
        click(scheduleSessionBtn, "Schedule a Session button");
    }

    // ── Matches ───────────────────────────────────────────────────────────────

    public boolean isMatchesSectionVisible() {
        return isDisplayed(matchesSection);
    }

    public int getMatchCardCount() {
        return matchCards.size();
    }

    public void clickFirstMatchButton() {
        if (!matchButtons.isEmpty()) {
            click(matchButtons.get(0), "First Match button");
        }
    }

    // ── Calendar Section ──────────────────────────────────────────────────────

    public boolean isCalendarSectionVisible() {
        return isDisplayed(calendarSection);
    }

    public void clickCalendarDay(int dayIndex) {
        if (dayIndex < calendarDays.size()) {
            click(calendarDays.get(dayIndex), "Calendar day " + dayIndex);
        }
    }

    public String getEmptyStateMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(emptyStateMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean isDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
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
