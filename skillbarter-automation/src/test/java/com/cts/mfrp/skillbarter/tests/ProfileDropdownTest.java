package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProfileDropdownPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Test class for Profile Dropdown Menu.
 *
 * Scenario : TS_007 – Verify Profile Dropdown Menu Options and Behaviour
 * Test Cases : TC_036, TC_037, TC_039
 * Group      : profile-dropdown, regression
 */
public class ProfileDropdownTest extends BaseTest {

    private ProfileDropdownPage dropdownPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenDropdown() {
        // Direct-form login (same pattern as CalendarTest) — resilient to
        // homepage CTA churn.
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

        // Topbar hydrates a few seconds after the URL flips; wait for the
        // user button to be clickable rather than racing it with a Thread.sleep.
        WebDriverWait topbarWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement profileTrigger = topbarWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'user-btn')] | //*[contains(@class,'avatar')]")));
        profileTrigger.click();

        // Wait for dropdown to actually render before clicks fire.
        topbarWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space()='Profile'] | //button[normalize-space()='Log Out']")));

        dropdownPage = new ProfileDropdownPage(driver);
    }

    @Test(testName = "TC_033", description = "Profile option navigates to profile page")
    public void tc036_profileOptionRedirects() {
        String urlBefore = getCurrentUrl();
        dropdownPage.clickProfile();

        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> !d.getCurrentUrl().equals(urlBefore));

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("profile") && !url.contains("saved"),
            "Did not navigate to profile page. URL: " + url
        );
    }

    @Test(testName = "TC_034", description = "Saved Profiles option redirects to saved list")
    public void tc037_savedProfilesOptionRedirects() {
        String urlBefore = getCurrentUrl();
        dropdownPage.clickSavedProfiles();

        // Replaces a hard 19s sleep — wait up to 30s for the URL to actually
        // contain 'saved'. Returns as soon as it does.
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> d.getCurrentUrl().contains("saved")
                         && !d.getCurrentUrl().equals(urlBefore));

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("saved"),
            "Did not navigate to Saved Profiles. URL: " + url
        );
    }

    @Test(testName = "TC_035", description = "Log Out terminates session and redirects to Sign In")
    public void tc039_logOutTerminatesSession() {
        dropdownPage.clickLogout();

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            String u = d.getCurrentUrl();
            return u.contains("signin") || u.contains("login") || u.equals(AppConstants.BASE_URL);
        });

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("signin") || url.contains("login") || url.equals(AppConstants.BASE_URL),
            "Did not return to Sign In / home after logout. URL: " + url
        );
    }
}
