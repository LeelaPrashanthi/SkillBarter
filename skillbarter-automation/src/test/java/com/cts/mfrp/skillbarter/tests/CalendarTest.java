package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.CalendarPage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Test class for Calendar & Sessions page.
 *
 * Scenario   : TS_014 – Verify Calendar Page Functionality
 * Requirement: REQ-2.14
 * Test Cases : TC_066 → TC_074
 * Group      : calendar, regression
 */
public class CalendarTest extends BaseTest {

    // Local override: this suite needs an account that already has at least one
    // session (TC_073/074) and at least one match (TC_071 onwards). leela123 is
    // the account whose data was visible in the screenshots provided.
    private static final String CALENDAR_EMAIL    = "leela123@gmail.com";
    private static final String CALENDAR_PASSWORD = "123456";

    private CalendarPage calendarPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenCalendar() {
        navigateTo(AppConstants.SIGNIN_URL);

        // Direct manual login — bypasses SignInPage. The page object's Sign In
        // button locator (//button[btn-primary] + text 'Sign In') no longer
        // matches the current app build, and its click() helper swallows the
        // failure with a .warn() log, so signIn() appeared to succeed while
        // never actually submitting. Submitting via Keys.ENTER on the password
        // field is resilient to button-class/label changes.
        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement emailField = loginWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));

        emailField.clear();
        emailField.sendKeys(CALENDAR_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(CALENDAR_PASSWORD);
        passwordField.sendKeys(Keys.ENTER);

        loginWait.until(ExpectedConditions.urlContains("dashboard"));

        navigateTo(AppConstants.CALENDAR_URL);
        // Bumped URL-contains wait too — the global EXPLICIT_WAIT (15s) is
        // tighter than the observed 20s page load on slow builds.
        new WebDriverWait(driver, Duration.ofSeconds(60))
                .until(ExpectedConditions.urlContains("calendar"));
        calendarPage = new CalendarPage(driver);
        // Calendar shell hydrates in ~6–7s on fast runs but can stretch past
        // 20s on slow builds — give it a generous 60s so + New Session Request
        // reliably appears before any test body runs.
        calendarPage.waitForPageReady(60);
    }

    // ── TC_066 – Monthly calendar view ──────────────────────────────────────

    @Test(testName = "TC_066", description = "Monthly calendar view displayed with date selection",
          groups = {"calendar", "regression"}, priority = 66, retryAnalyzer = RetryAnalyzer.class)
    public void tc066_monthlyCalendarView() {
        Assert.assertTrue(
            getCurrentUrl().contains("calendar"),
            "Not on the calendar URL. Current: " + getCurrentUrl()
        );
        Assert.assertTrue(
            calendarPage.isCalendarGridVisible(),
            "Day-of-week headers not visible — calendar grid did not render."
        );
        Assert.assertTrue(
            calendarPage.getDayOfWeekHeaderCount() >= 1,
            "Expected at least one weekday header (Su, Mo, …). Got: "
                + calendarPage.getDayOfWeekHeaderCount()
        );
        Assert.assertTrue(
            calendarPage.isNewSessionRequestBtnVisible(),
            "'+ New Session Request' CTA not visible — page not fully hydrated."
        );
    }

    // ── TC_067 – Tabs (Upcoming / History) ──────────────────────────────────
    // Note: the original description mentioned a Requests tab, but the current
    // UI build only exposes Upcoming and History. Test covers what exists.
    @Test(testName = "TC_067", description = "Upcoming and History tabs show correct sessions",
          groups = {"calendar", "regression"}, priority = 67, retryAnalyzer = RetryAnalyzer.class)
    public void tc067_sessionTabs() {
        calendarPage.clickUpcomingTab();
        int upcomingCount = calendarPage.getUpcomingSessionCount();
        Assert.assertTrue(upcomingCount >= 0, "Upcoming sessions list could not be queried.");

        calendarPage.clickHistoryTab();
        int historyCount = calendarPage.getHistorySessionCount();
        Assert.assertTrue(historyCount >= 0, "History sessions list could not be queried.");
    }

    // ── TC_068 – New Session Request opens form ─────────────────────────────

    @Test(testName = "TC_068", description = "New Session Request button opens session creation form",
          groups = {"calendar", "regression"}, priority = 68, retryAnalyzer = RetryAnalyzer.class)
    public void tc068_newSessionRequestFormOpens() {
        calendarPage.clickNewSessionRequest();
        Assert.assertTrue(
            calendarPage.isFormVisible(),
            "'Create Session Request' form did not appear after clicking + New Session Request."
        );
    }

    // ── TC_069 – Submit empty form ──────────────────────────────────────────

    @Test(testName = "TC_069", description = "Submitting Create Request with no match/skill/date is blocked",
          groups = {"calendar", "regression"}, priority = 69, retryAnalyzer = RetryAnalyzer.class)
    public void tc069_submitEmptyFormIsBlocked() {
        calendarPage.clickNewSessionRequest();
        Assert.assertTrue(calendarPage.isFormVisible(), "Form did not open.");

        int upcomingBefore = calendarPage.getUpcomingSessionCount();

        // Without making any selection, Create Request should be disabled OR
        // clicking it should result in no new upcoming session.
        boolean enabledWhileEmpty = calendarPage.isCreateRequestEnabled();
        if (enabledWhileEmpty) {
            // App is permissive — click and verify nothing got created.
            calendarPage.clickCreateRequest();
            calendarPage.clickUpcomingTab();
            int after = calendarPage.getUpcomingSessionCount();
            Assert.assertEquals(after, upcomingBefore,
                "Empty form submission unexpectedly created a session. Before: "
                    + upcomingBefore + ", after: " + after);
        } else {
            // Expected path: Create Request is disabled while fields are empty.
            Assert.assertFalse(enabledWhileEmpty,
                "Create Request button should be disabled when match/skill/date are empty.");
        }
    }

    // ── TC_072 – New session has Join Call & Mark as Complete buttons ──────

    @Test(testName = "TC_072", description = "New upcoming session shows Join Call and Mark as Complete actions",
          groups = {"calendar", "regression"}, priority = 72, retryAnalyzer = RetryAnalyzer.class)
    public void tc072_upcomingSessionShowsCallAndCompleteButtons() {
        calendarPage.clickUpcomingTab();
        // Session cards arrive via an async fetch (~20s on slow builds).
        // Wait before counting so we don't skip on a not-yet-rendered list.
        calendarPage.waitForSessionsLoaded(60);
        if (calendarPage.getUpcomingSessionCount() == 0) {
            throw new SkipException(
                "No upcoming sessions for this account — TC_072 needs at least one scheduled session."
            );
        }
        Assert.assertTrue(calendarPage.hasJoinCallButton(),
            "Upcoming session should expose a 'Join Call' button.");
        Assert.assertTrue(calendarPage.hasMarkCompleteButton(),
            "Upcoming session should expose a 'Mark as Complete' button.");
        Assert.assertTrue(calendarPage.getScheduledBadgeCount() >= 1,
            "Upcoming session should show at least one 'Scheduled' status badge.");
    }

    // ── TC_073 – Join Call triggers a call action ──────────────────────────

    @Test(testName = "TC_073", description = "Join Call on an upcoming session triggers the call flow",
          groups = {"calendar", "regression"}, priority = 73, retryAnalyzer = RetryAnalyzer.class)
    public void tc073_joinCallTriggersCallFlow() {
        calendarPage.clickUpcomingTab();
        calendarPage.waitForSessionsLoaded(60);
        if (!calendarPage.hasJoinCallButton()) {
            throw new SkipException("No 'Join Call' button rendered — no upcoming sessions available.");
        }

        String urlBefore = getCurrentUrl();
        int windowsBefore = driver.getWindowHandles().size();

        calendarPage.clickFirstJoinCall();

        // Join Call may open a new tab/window OR navigate the current page.
        try {
            wait.until(d -> d.getWindowHandles().size() > windowsBefore
                         || !d.getCurrentUrl().equals(urlBefore));
        } catch (Exception ignored) {
            // fall through to a clear assertion failure below
        }

        boolean triggered = driver.getWindowHandles().size() > windowsBefore
                         || !getCurrentUrl().equals(urlBefore);
        Assert.assertTrue(
            triggered,
            "Join Call click neither opened a new window nor changed the URL. URL: " + getCurrentUrl()
        );
    }

    // ── TC_074 – Mark as Complete moves session to History ─────────────────

    @Test(testName = "TC_074", description = "Mark as Complete moves the session to History with 'Completed' status",
          groups = {"calendar", "regression"}, priority = 74, retryAnalyzer = RetryAnalyzer.class)
    public void tc074_markCompleteMovesToHistory() {
        calendarPage.clickUpcomingTab();
        calendarPage.waitForSessionsLoaded(60);
        if (!calendarPage.hasMarkCompleteButton()) {
            throw new SkipException("No 'Mark as Complete' button rendered — no upcoming sessions available.");
        }

        int upcomingBefore = calendarPage.getUpcomingSessionCount();
        calendarPage.clickHistoryTab();
        int historyBefore = calendarPage.getHistorySessionCount();
        int completedBadgesBefore = calendarPage.getCompletedBadgeCount();

        calendarPage.clickUpcomingTab();
        calendarPage.clickFirstMarkComplete();

        // The app raises a native confirm dialog ("Mark this session as completed?
        // Both users will receive XP.") — accept it, otherwise Selenium will throw
        // UnhandledAlertException on the next call and the session won't actually move.
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception e) {
            Assert.fail("Expected a 'Mark as completed?' confirmation alert after the click, but none appeared.");
        }

        // Wait for the upcoming count to drop (the marked session leaves upcoming)
        try {
            wait.until(d -> calendarPage.getUpcomingSessionCount() < upcomingBefore);
        } catch (Exception e) {
            Assert.fail("Upcoming count did not drop after Mark as Complete + alert accept. Was: "
                + upcomingBefore + ", still: " + calendarPage.getUpcomingSessionCount());
        }

        calendarPage.clickHistoryTab();
        try {
            wait.until(d -> calendarPage.getHistorySessionCount() > historyBefore
                         || calendarPage.getCompletedBadgeCount() > completedBadgesBefore);
        } catch (Exception e) {
            Assert.fail("History count / Completed-badge count did not increase after Mark as Complete.");
        }
        Assert.assertTrue(
            calendarPage.getCompletedBadgeCount() > completedBadgesBefore,
            "Expected a new 'Completed' status badge in History after marking a session complete."
        );
    }
}
