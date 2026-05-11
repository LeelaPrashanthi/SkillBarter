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
 * Page Object for the Calendar page.
 * Covers TC_066 – TC_068 (TS_014).
 */
public class CalendarPage {

    private static final Logger log = LogManager.getLogger(CalendarPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='page-title']")
    private WebElement pageTitle;

    @FindBy(css = "[class*='calendar-grid'], [class*='monthly-view'], [class*='calendar-body']")
    private WebElement calendarGrid;

    @FindBy(css = "[class*='calendar'] td, [class*='day-cell'], [class*='cal-day']")
    private List<WebElement> calendarDays;

    @FindBy(css = "button[class*='upcoming'], [class*='tab-upcoming'], " +
            "[role='tab']:nth-child(1)")
    private WebElement upcomingTab;

    @FindBy(css = "button[class*='requests'], [class*='tab-requests'], " +
            "[role='tab']:nth-child(2)")
    private WebElement requestsTab;

    @FindBy(css = "button[class*='history'], [class*='tab-history'], " +
            "[role='tab']:nth-child(3)")
    private WebElement historyTab;

    @FindBy(css = "[class*='session-item'], [class*='session-card'], [class*='upcoming-item']")
    private List<WebElement> sessionItems;

    @FindBy(css = "button[class*='new-session'], a[class*='new-session'], " +
            "button[class*='request-session']")
    private WebElement newSessionRequestBtn;

    @FindBy(css = "[class*='session-form'], [class*='booking-form'], [class*='new-session-form']")
    private WebElement sessionForm;

    public CalendarPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isCalendarGridVisible() {
        return isDisplayed(calendarGrid);
    }

    public void clickCalendarDay(int index) {
        if (index < calendarDays.size()) click(calendarDays.get(index), "Calendar day " + index);
    }

    public void clickUpcomingTab() { click(upcomingTab, "Upcoming tab"); }
    public void clickRequestsTab() { click(requestsTab, "Requests tab"); }
    public void clickHistoryTab()  { click(historyTab, "History tab"); }

    public int getSessionCount() { return sessionItems.size(); }

    public void clickNewSessionRequest() {
        click(newSessionRequestBtn, "New Session Request button");
    }

    public boolean isSessionFormVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(sessionForm)).isDisplayed();
        } catch (Exception e) { return false; }
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
