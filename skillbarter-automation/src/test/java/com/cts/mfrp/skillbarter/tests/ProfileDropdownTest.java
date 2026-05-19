package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProfileDropdownPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

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
        navigateTo(AppConstants.BASE_URL);

        List<WebElement> entryBtns = driver.findElements(By.xpath(
                "//a[contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'sign in') "
                + "or contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'get started')] "
                + "| //button[contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'sign in') "
                + "or contains(translate(normalize-space(.), 'SIGNINGETSTARD', 'signingetstard'),'get started')]"));
        for (WebElement btn : entryBtns) {
            try { if (btn.isDisplayed()) { btn.click(); break; } } catch (Exception ignored) {}
        }

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email']")));
        emailField.sendKeys(AppConstants.VALID_EMAIL);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='password' or @name='password']")));
        passwordField.sendKeys(AppConstants.VALID_PASSWORD);

        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (Exception ignored) {
            passwordField.sendKeys(Keys.RETURN);
        }

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("signin")));

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Click the user button in the top-right to open the dropdown.
        WebElement profileTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'user-btn')] | //*[contains(@class,'avatar')]")));
        profileTrigger.click();

        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        dropdownPage = new ProfileDropdownPage(driver);
    }

    @Test(testName = "TC_036", description = "Profile option navigates to profile page")
    public void tc036_profileOptionRedirects() {
        dropdownPage.clickProfile();
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("profile") && !url.contains("saved"),
            "Did not navigate to profile page. URL: " + url
        );
    }

    @Test(testName = "TC_037", description = "Saved Profiles option redirects to saved list")
    public void tc037_savedProfilesOptionRedirects() {
        dropdownPage.clickSavedProfiles();
        try { Thread.sleep(19000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("saved"),
            "Did not navigate to Saved Profiles. URL: " + url
        );
    }

    @Test(testName = "TC_039", description = "Log Out terminates session and redirects to Sign In")
    public void tc039_logOutTerminatesSession() {
        dropdownPage.clickLogout();
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("signin") || url.contains("login") || url.equals(AppConstants.BASE_URL),
            "Did not return to Sign In / home after logout. URL: " + url
        );
    }
}
