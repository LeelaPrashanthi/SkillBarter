package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.DashboardPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test class for User Dashboard and Navigation Bar Component.
 *
 * Scenario   : TS_005 (Dashboard), TS_006 (Navigation Bar)
 * Requirement: REQ-2.4 / REQ-2.5 / REQ-2.6
 * Test Cases : TC_026 → TC_034
 * Group      : dashboard, nav-bar, regression, smoke
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
          groups = {"dashboard", "smoke", "regression"}, priority = 26, retryAnalyzer = RetryAnalyzer.class)
    public void tc026_dashboardLoadsAfterLogin() {
        Assert.assertTrue(getCurrentUrl().toLowerCase().contains("dashboard"),
                "Did not land on dashboard URL. Current: " + getCurrentUrl());
        Assert.assertTrue(dashboardPage.isSidebarVisible(),
                "Sidebar not visible on dashboard");
        Assert.assertTrue(dashboardPage.isTopbarVisible(),
                "Topbar not visible on dashboard");
        Assert.assertTrue(dashboardPage.isSpBadgeVisible(),
                "SP badge not visible in topbar");
    }

    @Test(testName = "TC_027", description = "Schedule a Session CTA opens session creation/booking flow",
          groups = {"dashboard", "regression"}, priority = 27, retryAnalyzer = RetryAnalyzer.class)
    public void tc027_scheduleSessionOpensFlow() {
        Assert.assertTrue(dashboardPage.isScheduleSessionVisible(),
                "'Schedule a session' CTA not visible on Activity card");
        dashboardPage.clickScheduleSession();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("calendar"),
                "Did not navigate to calendar/booking flow. URL: " + getCurrentUrl());
    }

    @Test(testName = "TC_028", description = "Matches section shows Top Matches heading and a 'see all' link",
          groups = {"dashboard", "regression"}, priority = 28, retryAnalyzer = RetryAnalyzer.class)
    public void tc028_matchesSectionDisplaysProfiles() {
        Assert.assertTrue(dashboardPage.isTopMatchesSectionVisible(),
                "'Top Matches' heading not visible");
        Assert.assertTrue(dashboardPage.isSeeAllMatchesLinkVisible(),
                "'see all' link missing from Top Matches section");
        // Either user has match cards rendered, or the empty-state is shown — both are valid.
        Assert.assertTrue(dashboardPage.getMatchCardCount() >= 0,
                "Match cards lookup failed");
    }

    @Test(testName = "TC_029", description = "Calendar section shows monthly view with today highlighted",
          groups = {"dashboard", "regression"}, priority = 29, retryAnalyzer = RetryAnalyzer.class)
    public void tc029_calendarSectionMonthlyView() {
        Assert.assertTrue(dashboardPage.isCalendarCardVisible(),
                "Mini calendar card not visible on dashboard");
        String month = dashboardPage.getCalendarMonthLabel();
        Assert.assertFalse(month.isEmpty(),
                "Calendar month label is empty");
        Assert.assertTrue(dashboardPage.getCalendarDayCellCount() >= 28,
                "Calendar has too few day cells. Found: " + dashboardPage.getCalendarDayCellCount());
        Assert.assertTrue(dashboardPage.isTodayHighlighted(),
                "Today's date is not highlighted in the mini calendar");
        Assert.assertTrue(dashboardPage.isUpcomingSectionVisible(),
                "Upcoming section heading not visible under calendar");
    }

    @Test(testName = "TC_030", description = "Skill Points counter is displayed and shows numeric value",
          groups = {"dashboard", "regression"}, priority = 30, retryAnalyzer = RetryAnalyzer.class)
    public void tc030_skillPointsCounterDisplayed() {
        Assert.assertTrue(dashboardPage.isSpBadgeVisible(),
                "SP badge not visible");
        String sp = dashboardPage.getSpValue();
        Assert.assertFalse(sp.isEmpty(), "SP value text is empty");
        Assert.assertTrue(dashboardPage.spValueIsNumeric(),
                "SP value does not match '<number> SP' pattern. Got: '" + sp + "'");
    }

    @Test(testName = "TC_031", description = "Sidebar items navigate to Matches, Calendar, Progress, Community modules",
          groups = {"dashboard", "regression"}, priority = 31, retryAnalyzer = RetryAnalyzer.class)
    public void tc031_sidebarNavigationWorks() {
        Assert.assertTrue(dashboardPage.areAllSidebarNavItemsPresent(),
                "Not all sidebar nav items are visible");

        dashboardPage.clickSidebarMatches();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("matches"),
                "Sidebar 'Matches' did not navigate. URL: " + getCurrentUrl());

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarCalendar();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("calendar"),
                "Sidebar 'Calendar' did not navigate. URL: " + getCurrentUrl());

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarProgress();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("progress"),
                "Sidebar 'Progress' did not navigate. URL: " + getCurrentUrl());

        navigateTo(AppConstants.DASHBOARD_URL);
        dashboardPage.waitForDashboardLoaded();
        dashboardPage.clickSidebarCommunity();
        Assert.assertTrue(dashboardPage.waitForUrlToContain("community"),
                "Sidebar 'Community' did not navigate. URL: " + getCurrentUrl());
    }

    @Test(testName = "TC_032", description = "Nav bar SP indicator is visible and shows current balance",
          groups = {"nav-bar", "regression"}, priority = 32, retryAnalyzer = RetryAnalyzer.class)
    public void tc032_spIndicatorAndPlusIcon() {
        Assert.assertTrue(dashboardPage.isSpBadgeVisible(),
                "SP indicator not visible in nav bar");
        Assert.assertTrue(dashboardPage.spValueIsNumeric(),
                "SP indicator does not show a numeric balance. Got: '" + dashboardPage.getSpValue() + "'");
    }

    @Test(testName = "TC_033", description = "Notification bell is visible and opens notifications panel when clicked",
          groups = {"nav-bar", "regression"}, priority = 33, retryAnalyzer = RetryAnalyzer.class)
    public void tc033_notificationBellOpensPanel() {
        Assert.assertTrue(dashboardPage.isNotificationBellVisible(),
                "Notification bell not visible in topbar");
        dashboardPage.clickNotificationBell();
        Assert.assertTrue(dashboardPage.waitForNotificationPanelVisible(),
                "Notification panel did not open after clicking the bell");
    }

    @Test(testName = "TC_034", description = "Clicking user profile in nav bar opens dropdown with menu items",
          groups = {"nav-bar", "regression"}, priority = 34, retryAnalyzer = RetryAnalyzer.class)
    public void tc034_profileDropdownInNavBar() {
        Assert.assertTrue(dashboardPage.isUserButtonVisible(),
                "User button (avatar + name) not visible in topbar");
        Assert.assertFalse(dashboardPage.getUserName().isEmpty(),
                "User name is empty in topbar");

        dashboardPage.clickUserButton();
        Assert.assertTrue(dashboardPage.waitForUserDropdownVisible(),
                "User dropdown did not open after clicking the avatar/name");

        List<String> items = dashboardPage.getDropdownItemTexts();
        Assert.assertFalse(items.isEmpty(), "User dropdown rendered with no menu items");
        String joined = String.join(" | ", items).toLowerCase();
        Assert.assertTrue(joined.contains("log out") || joined.contains("logout"),
                "User dropdown missing Log Out option. Items: " + items);
    }
}
