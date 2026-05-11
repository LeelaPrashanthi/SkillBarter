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
 * Page Object for the Community page.
 * Covers TC_074 – TC_077 (TS_016).
 */
public class CommunityPage {

    private static final Logger log = LogManager.getLogger(CommunityPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='page-title']")
    private WebElement pageTitle;

    @FindBy(css = "[class*='story-card'], [class*='user-story'], [class*='story-item']")
    private List<WebElement> storyCards;

    @FindBy(css = "button[class*='share-story'], [class*='share-story-btn']")
    private WebElement shareStoryBtn;

    @FindBy(css = "[class*='story-form'], [class*='submit-story-form']")
    private WebElement storyForm;

    @FindBy(css = "input[name='title'], [class*='story-title-input']")
    private WebElement storyTitleInput;

    @FindBy(css = "textarea[name='content'], [class*='story-content-input']")
    private WebElement storyContentInput;

    @FindBy(css = "button[type='submit'][class*='story'], [class*='submit-story-btn']")
    private WebElement submitStoryBtn;

    @FindBy(css = "[class*='top-contributor'], [class*='contributors-section'] [class*='item']")
    private List<WebElement> topContributors;

    @FindBy(css = "button[class*='explore'], a[class*='explore-discussions']")
    private WebElement exploreDiscussionsBtn;

    public CommunityPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean areStoriesDisplayed() {
        return !storyCards.isEmpty();
    }

    public int getStoryCount() { return storyCards.size(); }

    public String getFirstStoryText() {
        try { return storyCards.get(0).getText().trim(); } catch (Exception e) { return ""; }
    }

    public void clickShareYourStory() {
        click(shareStoryBtn, "Share Your Story button");
    }

    public boolean isStoryFormVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(storyForm)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public void submitStory(String title, String content) {
        clearAndType(storyTitleInput, title, "Story title");
        clearAndType(storyContentInput, content, "Story content");
        click(submitStoryBtn, "Submit story button");
    }

    public boolean isTopContributorsSectionVisible() {
        return !topContributors.isEmpty() && topContributors.get(0).isDisplayed();
    }

    public void clickExploreDiscussions() {
        click(exploreDiscussionsBtn, "Explore Discussions");
    }

    private void clearAndType(WebElement el, String text, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(el));
            el.clear();
            el.sendKeys(text);
        } catch (Exception e) {
            log.warn("clearAndType failed on '{}': {}", name, e.getMessage());
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
