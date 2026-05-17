package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for the User Dashboard.
 * Covers TC_026 – TC_034 (TS_005 + TS_006).
 *
 * All locators use XPath. Multi-element / dynamic lookups go through
 * driver.findElements(...) so they always see fresh DOM state after
 * Angular re-renders.
 */
public class DashboardPage {

    private static final Logger log = LogManager.getLogger(DashboardPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Sidebar (static, single elements) ─────────────────────────────────────
    @FindBy(xpath = "//*[contains(@class,'sidebar')]")
    private WebElement sidebar;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//*[contains(@class,'brand-name')]")
    private WebElement sidebarBrand;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/dashboard')]")
    private WebElement sidebarDashboard;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/matches')]")
    private WebElement sidebarMatches;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/calendar')]")
    private WebElement sidebarCalendar;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/chat')]")
    private WebElement sidebarChat;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/progress')]")
    private WebElement sidebarProgress;

    @FindBy(xpath = "//*[contains(@class,'sidebar')]//a[contains(@href,'/community')]")
    private WebElement sidebarCommunity;

    // ── Topbar ────────────────────────────────────────────────────────────────
    @FindBy(xpath = "//*[contains(@class,'topbar')]")
    private WebElement topbar;

    @FindBy(xpath = "//*[contains(@class,'sp-badge')]")
    private WebElement spBadge;

    @FindBy(xpath = "//*[contains(@class,'sp-value')]")
    private WebElement spValue;

    @FindBy(xpath = "//*[contains(@class,'topbar')]//button[contains(@class,'theme-btn')]")
    private WebElement themeToggleBtn;

    @FindBy(xpath = "//*[contains(@class,'topbar')]//button[contains(@class,'icon-btn') and not(contains(@class,'theme-btn'))]")
    private WebElement notificationBellBtn;

    @FindBy(xpath = "//*[contains(@class,'user-btn')]")
    private WebElement userBtn;

    @FindBy(xpath = "//*[contains(@class,'user-name')]")
    private WebElement userName;

    // ── Activity Card ─────────────────────────────────────────────────────────
    @FindBy(xpath = "//h2[normalize-space()='Activity']")
    private WebElement activityHeading;

    @FindBy(xpath = "//*[contains(@class,'empty')]//h3[contains(normalize-space(),'Ready for your first session')]")
    private WebElement activityEmptyHeading;

    @FindBy(xpath = "//a[normalize-space()='Schedule a session']")
    private WebElement scheduleSessionLink;

    // ── Top Matches Card ──────────────────────────────────────────────────────
    @FindBy(xpath = "//h2[normalize-space()='Top Matches']")
    private WebElement topMatchesHeading;

    @FindBy(xpath = "//a[contains(@class,'link') and contains(normalize-space(),'see all')]")
    private WebElement seeAllMatchesLink;

    // ── Mini Calendar ─────────────────────────────────────────────────────────
    @FindBy(xpath = "//*[contains(@class,'cal-card')]")
    private WebElement calendarCard;

    @FindBy(xpath = "//*[contains(@class,'calh')]//*[contains(@class,'ct')]")
    private WebElement calendarMonthLabel;

    @FindBy(xpath = "//*[contains(@class,'cal-card')]//*[contains(@class,'cd') and contains(@class,'today')]")
    private WebElement calendarTodayCell;

    @FindBy(xpath = "//*[contains(@class,'upcoming')]//h4[normalize-space()='Upcoming']")
    private WebElement upcomingHeading;

    // ── XPath By constants (re-queried each call) ─────────────────────────────
    private static final By SIDEBAR_NAV_ITEMS    = By.xpath("//*[contains(@class,'sidebar')]//a[contains(@class,'nav-item')]");
    private static final By DROPDOWN_PANEL       = By.xpath("//*[contains(@class,'user-btn')]//*[contains(@class,'dropdown')]");
    private static final By DROPDOWN_ITEMS       = By.xpath("//*[contains(@class,'user-btn')]//*[contains(@class,'dropdown')]//*[contains(@class,'dd-item')]");
    private static final By NOTIF_PANEL          = By.xpath("//*[contains(@class,'notif-panel')]");
    private static final By MATCH_CARDS          = By.xpath("//*[contains(@class,'mrow')]//*[contains(@class,'mc')]");
    private static final By CALENDAR_DAY_CELLS   = By.xpath("//*[contains(@class,'cal-card')]//*[contains(@class,'cd') and not(contains(@class,'other'))]");
    private static final By UPCOMING_ITEMS       = By.xpath("//*[contains(@class,'upcoming')]//*[contains(@class,'ui')]");
    private static final By ACTIVITY_STAT_VALUES = By.xpath("//*[contains(@class,'stat')]//strong");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Page-level waits ──────────────────────────────────────────────────────

    public boolean waitForDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(topbar));
            wait.until(ExpectedConditions.visibilityOf(sidebar));
            wait.until(ExpectedConditions.visibilityOf(spBadge));
            return true;
        } catch (Exception e) {
            log.warn("Dashboard not loaded within wait window: {}", e.getMessage());
            return false;
        }
    }

    public boolean isSidebarVisible() {
        return isVisibleWithWait(sidebar);
    }

    public boolean isTopbarVisible() {
        return isVisibleWithWait(topbar);
    }

    // ── Sidebar nav ───────────────────────────────────────────────────────────

    public boolean areAllSidebarNavItemsPresent() {
        return isVisibleWithWait(sidebarDashboard)
                && isVisibleWithWait(sidebarMatches)
                && isVisibleWithWait(sidebarCalendar)
                && isVisibleWithWait(sidebarChat)
                && isVisibleWithWait(sidebarProgress)
                && isVisibleWithWait(sidebarCommunity);
    }

    public int getSidebarNavItemCount() {
        return driver.findElements(SIDEBAR_NAV_ITEMS).size();
    }

    public void clickSidebarDashboard() { click(sidebarDashboard, "Sidebar: Dashboard"); }
    public void clickSidebarMatches()   { click(sidebarMatches,   "Sidebar: Matches"); }
    public void clickSidebarCalendar()  { click(sidebarCalendar,  "Sidebar: Calendar"); }
    public void clickSidebarChat()      { click(sidebarChat,      "Sidebar: Chat"); }
    public void clickSidebarProgress()  { click(sidebarProgress,  "Sidebar: Progress"); }
    public void clickSidebarCommunity() { click(sidebarCommunity, "Sidebar: Community"); }

    public boolean waitForUrlToContain(String fragment) {
        try {
            return wait.until(ExpectedConditions.urlContains(fragment));
        } catch (Exception e) {
            return false;
        }
    }

    // ── SP badge ──────────────────────────────────────────────────────────────

    public boolean isSpBadgeVisible() {
        return isVisibleWithWait(spBadge);
    }

    public String getSpValue() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(spValue)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean spValueIsNumeric() {
        String text = getSpValue();
        return text.matches("^\\d+\\s*SP$");
    }

    // ── Theme / notification bell ─────────────────────────────────────────────

    public boolean isThemeToggleVisible() {
        return isVisibleWithWait(themeToggleBtn);
    }

    public boolean isNotificationBellVisible() {
        return isVisibleWithWait(notificationBellBtn);
    }

    public void clickNotificationBell() {
        click(notificationBellBtn, "Notification bell");
    }

    public boolean waitForNotificationPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(NOTIF_PANEL)) != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ── User button / dropdown ────────────────────────────────────────────────

    public boolean isUserButtonVisible() {
        return isVisibleWithWait(userBtn);
    }

    public String getUserName() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(userName)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void clickUserButton() {
        click(userBtn, "User button (topbar)");
    }

    public boolean waitForUserDropdownVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(DROPDOWN_PANEL)) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getDropdownItemTexts() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(DROPDOWN_PANEL));
            return driver.findElements(DROPDOWN_ITEMS).stream()
                    .map(el -> el.getText().trim())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    public boolean isActivitySectionVisible() {
        return isVisibleWithWait(activityHeading);
    }

    public boolean isActivityEmptyState() {
        return isVisibleWithWait(activityEmptyHeading);
    }

    public boolean isScheduleSessionVisible() {
        return isVisibleWithWait(scheduleSessionLink);
    }

    public void clickScheduleSession() {
        click(scheduleSessionLink, "Schedule a session link");
    }

    public int getStatValueByOffset(int index) {
        try {
            List<WebElement> values = driver.findElements(ACTIVITY_STAT_VALUES);
            if (index >= values.size()) return -1;
            return Integer.parseInt(values.get(index).getText().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    // ── Top Matches ───────────────────────────────────────────────────────────

    public boolean isTopMatchesSectionVisible() {
        return isVisibleWithWait(topMatchesHeading);
    }

    public boolean isSeeAllMatchesLinkVisible() {
        return isVisibleWithWait(seeAllMatchesLink);
    }

    public void clickSeeAllMatches() {
        click(seeAllMatchesLink, "See all matches link");
    }

    public int getMatchCardCount() {
        return driver.findElements(MATCH_CARDS).size();
    }

    // ── Mini Calendar ─────────────────────────────────────────────────────────

    public boolean isCalendarCardVisible() {
        return isVisibleWithWait(calendarCard);
    }

    public String getCalendarMonthLabel() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(calendarMonthLabel)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isTodayHighlighted() {
        return isVisibleWithWait(calendarTodayCell);
    }

    public int getCalendarDayCellCount() {
        return driver.findElements(CALENDAR_DAY_CELLS).size();
    }

    public boolean isUpcomingSectionVisible() {
        return isVisibleWithWait(upcomingHeading);
    }

    public int getUpcomingItemCount() {
        return driver.findElements(UPCOMING_ITEMS).size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isVisibleWithWait(WebElement el) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(el)).isDisplayed();
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
