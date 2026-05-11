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
 * Page Object for the Matches page.
 * Covers TC_056 – TC_060 (TS_012).
 */
public class MatchesPage {

    private static final Logger log = LogManager.getLogger(MatchesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='title'], [class*='page-heading']")
    private WebElement pageTitle;

    @FindBy(css = "button[class*='find-match'], [class*='find-new-match'], button[class*='find']")
    private WebElement findNewMatchesBtn;

    @FindBy(css = "button[class*='my-match'], a[class*='my-match'], [class*='mymatches-tab']")
    private WebElement myMatchesTab;

    @FindBy(css = "[class*='match-card'], [class*='profile-card'], [class*='user-card']")
    private List<WebElement> matchCards;

    @FindBy(css = "[class*='skill-filter'], select[class*='skill'], [class*='filter-skill']")
    private WebElement skillFilterDropdown;

    @FindBy(css = "input[type='checkbox'][class*='availability'], " +
            "[class*='instant-avail'] input, [class*='avail-toggle']")
    private WebElement instantAvailabilityToggle;

    @FindBy(css = "button[class*='connect']:first-of-type, [class*='match-card'] button[class*='connect']")
    private WebElement firstConnectBtn;

    @FindBy(css = "button[class*='connect'], [class*='match-card'] button")
    private List<WebElement> connectButtons;

    public MatchesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public void clickFindNewMatches() {
        click(findNewMatchesBtn, "Find New Matches");
    }

    public void clickMyMatches() {
        click(myMatchesTab, "My Matches tab");
    }

    public int getMatchCardCount() {
        return matchCards.size();
    }

    public boolean areMatchCardsDisplayed() {
        return !matchCards.isEmpty() && matchCards.get(0).isDisplayed();
    }

    public void selectSkillFilter(String skillText) {
        try {
            click(skillFilterDropdown, "Skill filter");
            // After opening, find the option and click it
            driver.findElement(org.openqa.selenium.By.xpath(
                    "//option[contains(text(),'" + skillText + "')] | " +
                    "//*[contains(@class,'option') and contains(text(),'" + skillText + "')]"
            )).click();
            log.debug("Selected skill filter: {}", skillText);
        } catch (Exception e) {
            log.warn("selectSkillFilter failed: {}", e.getMessage());
        }
    }

    public void toggleInstantAvailability() {
        click(instantAvailabilityToggle, "Instant Availability toggle");
    }

    public boolean isInstantAvailabilityChecked() {
        try {
            return instantAvailabilityToggle.isSelected();
        } catch (Exception e) { return false; }
    }

    public void clickFirstConnectButton() {
        if (!connectButtons.isEmpty()) click(connectButtons.get(0), "First Connect button");
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
