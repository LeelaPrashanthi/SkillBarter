package com.cts.mfrp.skillbarter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Profile Dropdown Menu (TC_036 / TC_037 / TC_039).
 */
public class ProfileDropdownPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ProfileDropdownPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public void clickProfile() {
        clickOption("//a[normalize-space()='Profile']");
    }

    public void clickSavedProfiles() {
        clickOption("//a[normalize-space()='Saved Profiles']");
    }

    public void clickLogout() {
        clickOption("//button[normalize-space()='Log Out']");
    }

    /** Waits for the option to be clickable, then clicks via Actions (with JS fallback). */
    private void clickOption(String xpath) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        try {
            new Actions(driver).moveToElement(el).pause(150).click().perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
