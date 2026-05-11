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
 * Page Object for the Profile Dropdown Menu component.
 * Covers TC_035 – TC_040 (TS_007).
 */
public class ProfileDropdownPage {

    private static final Logger log = LogManager.getLogger(ProfileDropdownPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "[class*='profile-dropdown'], [class*='dropdown-menu'], [class*='user-menu']")
    private WebElement dropdownMenu;

    @FindBy(css = "[class*='dropdown'] a[href*='profile']:not([href*='saved']), " +
            "[class*='user-menu'] [class*='profile-link']")
    private WebElement profileOption;

    @FindBy(css = "[class*='dropdown'] a[href*='saved'], " +
            "[class*='user-menu'] [class*='saved-profile']")
    private WebElement savedProfilesOption;

    @FindBy(css = "[class*='dropdown'] a[href*='subscription'], " +
            "[class*='user-menu'] [class*='subscription']")
    private WebElement subscriptionsOption;

    @FindBy(css = "[class*='dropdown'] button[class*='logout'], " +
            "[class*='user-menu'] [class*='logout'], [class*='signout']")
    private WebElement logoutOption;

    @FindBy(css = "body")
    private WebElement body;

    public ProfileDropdownPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public boolean isDropdownOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(dropdownMenu)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isProfileOptionPresent()        { return isDisplayed(profileOption); }
    public boolean isSavedProfilesOptionPresent()  { return isDisplayed(savedProfilesOption); }
    public boolean isSubscriptionsOptionPresent()  { return isDisplayed(subscriptionsOption); }
    public boolean isLogoutOptionPresent()         { return isDisplayed(logoutOption); }

    public boolean areAllOptionsPresent() {
        return isProfileOptionPresent() && isSavedProfilesOptionPresent()
                && isSubscriptionsOptionPresent() && isLogoutOptionPresent();
    }

    public void clickProfile()        { click(profileOption, "Profile option"); }
    public void clickSavedProfiles()  { click(savedProfilesOption, "Saved Profiles option"); }
    public void clickSubscriptions()  { click(subscriptionsOption, "Subscriptions option"); }
    public void clickLogout()         { click(logoutOption, "Log Out option"); }

    public void clickOutside() {
        try {
            // Click on body area away from dropdown
            body.click();
        } catch (Exception e) {
            log.warn("clickOutside failed: {}", e.getMessage());
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

    private boolean isDisplayed(WebElement el) {
        try { return el.isDisplayed(); } catch (Exception e) { return false; }
    }
}
