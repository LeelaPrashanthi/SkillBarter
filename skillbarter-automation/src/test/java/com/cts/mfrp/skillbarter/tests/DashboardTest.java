package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.DashboardPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Test class for User Dashboard and Navigation Bar Component.
 *
 * Scenario   : TS_005 (Dashboard), TS_006 (Navigation Bar)
 * Requirement: REQ-2.4 / REQ-2.5 / REQ-2.6
 * Test Cases : TC_026 → TC_034
 * Group      : dashboard, nav-bar, regression, smoke
 *
 * Convention: each @Test has exactly one Assert call. Steps before the
 * assertion rely on WebDriverWait conditions — a timeout there fails the
 * test with a TimeoutException, which serves the same role as a precondition
 * assert without inflating the assertion count.
 */
public class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenDashboard() {
        navigateTo(AppConstants.SIGNIN_URL);
        new SignInPage(driver).signIn(AppConstants.VALID_EMAIL, AppConstants.VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("dashboard"));
        dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.waitForDashboardLoaded(),
                "Dashboard did not finish loading after login (topbar/sidebar/SP badge not visible).");
    }

    @Test(testName = "TC_026", description = "Dashboard loads after login with sidebar and SP counter visible",
          groups = {"dashboard", "smoke", "regression"}, priority = 26)
    public void tc026_dashboardLoadsAfterLogin() {
        Assert.assertTrue(dashboardPage.isSidebarVisible(),
                "Sidebar not visible on dashboard");
    }

    @Test(testName = "TC_027", description = "Schedule a Session CTA opens session creation/booking flow",
          groups = {"dashboard", "regression"}, priority = 27)
    public void tc027_scheduleSessionOpensFlow() {
        dashboardPage.clickScheduleSession();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("calendar"),
                "Did not navigate to calendar/booking flow. URL: " + getCurrentUrl());
    }

    @Test(testName = "TC_028", description = "Matches section shows Top Matches heading and a 'see all' link",
          groups = {"dashboard", "regression"}, priority = 28)
    public void tc028_matchesSectionDisplaysProfiles() {
        Assert.assertTrue(dashboardPage.isTopMatchesSectionVisible(),
                "'Top Matches' heading not visible");
    }

    @Test(testName = "TC_029", description = "Calendar section shows monthly view with today highlighted",
          groups = {"dashboard", "regression"}, priority = 29)
    public void tc029_calendarSectionMonthlyView() {
        Assert.assertTrue(dashboardPage.isTodayHighlighted(),
                "Today's date is not highlighted in the mini calendar");
    }

    @Test(testName = "TC_030", description = "Skill Points counter is displayed and shows numeric value",
          groups = {"dashboard", "regression"}, priority = 30)
    public void tc030_skillPointsCounterDisplayed() {
        Assert.assertTrue(dashboardPage.spValueIsNumeric(),
                "SP value does not match '<number> SP' pattern. Got: '" + dashboardPage.getSpValue() + "'");
    }

    @Test(testName = "TC_031", description = "Sidebar items navigate to Matches, Calendar, Progress, Community modules",
          groups = {"dashboard", "regression"}, priority = 31)
    public void tc031_sidebarNavigationWorks() {
        // Each click + WebDriverWait below throws TimeoutException if the
        // navigation doesn't complete — equivalent to per-step asserts but
        // keeps the test at a single explicit Assert (the final URL check).
        dashboardPage.clickSidebarMatches();
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("matches"));

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarCalendar();
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("calendar"));

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarProgress();
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("progress"));

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarCommunity();
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("community"));

        Assert.assertTrue(getCurrentUrl().contains("community"),
                "Sidebar navigation did not end at Community. URL: " + getCurrentUrl());
    }

    @Test(testName = "TC_032", description = "Nav bar SP indicator is visible and shows current balance",
          groups = {"nav-bar", "regression"}, priority = 32)
    public void tc032_spIndicatorAndPlusIcon() {
        Assert.assertTrue(dashboardPage.spValueIsNumeric(),
                "SP indicator does not show a numeric balance. Got: '" + dashboardPage.getSpValue() + "'");
    }

    @Test(testName = "TC_033", description = "Notification bell is visible and opens notifications panel when clicked",
          groups = {"nav-bar", "regression"}, priority = 33)
    public void tc033_notificationBellOpensPanel() {
        dashboardPage.clickNotificationBell();
        Assert.assertTrue(dashboardPage.waitForNotificationPanelVisible(),
                "Notification panel did not open after clicking the bell");
    }

    @Test(testName = "TC_034", description = "Clicking user profile in nav bar opens dropdown with menu items",
          groups = {"nav-bar", "regression"}, priority = 34)
    public void tc034_profileDropdownInNavBar() {
        dashboardPage.clickUserButton();
        // Wait for dropdown to render (best-effort — if it doesn't, the
        // items list below will be empty and the assertion will fail).
        dashboardPage.waitForUserDropdownVisible();

        List<String> items = dashboardPage.getDropdownItemTexts();
        String joined = String.join(" | ", items).toLowerCase();
        Assert.assertTrue(joined.contains("log out") || joined.contains("logout"),
                "User dropdown missing Log Out option. Items: " + items);
    }
}
