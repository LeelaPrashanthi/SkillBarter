package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
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
 * Covers TC_074 – TC_082 (TS_016).
 */
public class CommunityPage {

    private static final Logger log = LogManager.getLogger(CommunityPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//*[contains(@class,'comm-header')]//h1")
    private WebElement pageTitle;

    @FindBy(xpath = "//*[contains(@class,'comm-header')]//button[normalize-space()='Share Your Story']")
    private WebElement shareStoryBtn;

    @FindBy(xpath = "//*[contains(@class,'story-form')]")
    private WebElement storyForm;

    @FindBy(xpath = "//*[contains(@class,'story-form')]//input")
    private WebElement storyTitleInput;

    @FindBy(xpath = "//*[contains(@class,'story-form')]//textarea[contains(@class,'ta')]")
    private WebElement storyContentInput;

    @FindBy(xpath = "//*[contains(@class,'story-form')]//button[normalize-space()='Post Story']")
    private WebElement submitStoryBtn;

    @FindBy(xpath = "//*[contains(@class,'section-title') and normalize-space()='Top Contributors']")
    private WebElement topContributorsHeading;

    @FindBy(xpath = "//a[contains(@href,'discussion') or contains(@href,'forum')] | //button[contains(@class,'explore-discussions')]")
    private WebElement exploreDiscussionsBtn;

    // ── XPath By constants (used with driver.findElements for fresh DOM lookups) ──
    private static final By STORY_CARDS      = By.xpath("//*[contains(@class,'stories-grid')]//*[contains(@class,'story-card')]");
    private static final By CANCEL_BTNS      = By.xpath("//*[contains(@class,'comm-page')]//button[normalize-space()='Cancel']");
    private static final By CONTRIB_CARDS    = By.xpath("//*[contains(@class,'contrib-grid')]//*[contains(@class,'contrib-card')]");

    // ── Relative XPaths (used with WebElement.findElement(s) inside a story card) ──
    private static final By STORY_AUTHOR     = By.xpath(".//*[contains(@class,'sauthor')]");
    private static final By STORY_TITLE_H3   = By.xpath(".//*[contains(@class,'story-view')]//h3");
    private static final By STORY_CONTENT_P  = By.xpath(".//*[contains(@class,'story-view')]//p");
    private static final By STORY_EDIT_BTN   = By.xpath(".//*[contains(@class,'story-actions')]//button[not(contains(@class,'danger'))]");
    private static final By STORY_DELETE_BTN = By.xpath(".//*[contains(@class,'story-actions')]//button[contains(@class,'danger')]");
    private static final By STORY_EDIT_FORM  = By.xpath(".//*[contains(@class,'story-edit')]");
    private static final By EDIT_FORM_TITLE_INPUT   = By.xpath(".//input");
    private static final By EDIT_FORM_CONTENT_INPUT = By.xpath(".//textarea[contains(@class,'ta')]");
    private static final By EDIT_FORM_SAVE_BTN      = By.xpath(".//*[contains(@class,'story-actions')]//button[contains(@class,'btn-primary')]");

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
        return !driver.findElements(STORY_CARDS).isEmpty();
    }

    public int getStoryCount() {
        return driver.findElements(STORY_CARDS).size();
    }

    public String getFirstStoryAuthor() {
        try { return driver.findElements(STORY_CARDS).get(0).findElement(STORY_AUTHOR).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getFirstStoryTitle() {
        try { return driver.findElements(STORY_CARDS).get(0).findElement(STORY_TITLE_H3).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getFirstStoryContent() {
        try { return driver.findElements(STORY_CARDS).get(0).findElement(STORY_CONTENT_P).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public void clickShareYourStory() {
        click(shareStoryBtn, "Share Your Story button");
    }

    public boolean isStoryFormVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(storyForm)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    /**
     * Waits up to 15s for the Share Your Story form to close. Used by tests
     * that need a Selenium-native signal that the Post Story submit cycle
     * has finished — so assertions like "count unchanged" or "story present"
     * don't fire mid-flight. Returns true if the form closed, false on timeout.
     */
    public boolean waitForStoryFormClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'story-form')]")));
        } catch (Exception e) {
            return false;
        }
    }

    public int getCancelButtonCountInStoryForm() {
        try {
            wait.until(ExpectedConditions.visibilityOf(storyForm));
            return (int) driver.findElements(CANCEL_BTNS).stream().filter(WebElement::isDisplayed).count();
        } catch (Exception e) { return 0; }
    }

    public void submitStory(String title, String content) {
        clearAndType(storyTitleInput, title, "Story title");
        clearAndType(storyContentInput, content, "Story content");
        click(submitStoryBtn, "Submit story button");
    }

    public boolean storyExistsByTitle(String title) {
        return driver.findElements(STORY_CARDS).stream().anyMatch(card -> {
            try {
                return card.findElement(STORY_TITLE_H3).getText().trim().equals(title == null ? null : title.trim());
            } catch (Exception e) { return false; }
        });
    }

    public boolean waitForStoryByTitle(String title) {
        try {
            return wait.until(d -> d.findElements(STORY_CARDS).stream().anyMatch(card -> {
                try {
                    return card.findElement(STORY_TITLE_H3).getText().trim().equals(title == null ? null : title.trim());
                } catch (Exception e) { return false; }
            }));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForStoryAbsentByTitle(String title) {
        try {
            return wait.until(d -> d.findElements(STORY_CARDS).stream().noneMatch(card -> {
                try {
                    return card.findElement(STORY_TITLE_H3).getText().trim().equals(title == null ? null : title.trim());
                } catch (Exception e) { return false; }
            }));
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement findStoryCardByTitle(String title) {
        return driver.findElements(STORY_CARDS).stream().filter(card -> {
            try {
                return card.findElement(STORY_TITLE_H3).getText().trim().equals(title == null ? null : title.trim());
            } catch (Exception e) { return false; }
        }).findFirst().orElse(null);
    }

    public WebElement waitAndGetStoryCardByTitle(String title) {
        try {
            return wait.until(d -> d.findElements(STORY_CARDS).stream()
                    .filter(card -> {
                        try {
                            return card.findElement(STORY_TITLE_H3).getText().trim().equals(title == null ? null : title.trim());
                        } catch (Exception e) { return false; }
                    })
                    .findFirst()
                    .orElse(null));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEditButtonPresent(WebElement storyCard) {
        return storyCard != null && !storyCard.findElements(STORY_EDIT_BTN).isEmpty();
    }

    public boolean isDeleteButtonPresent(WebElement storyCard) {
        return storyCard != null && !storyCard.findElements(STORY_DELETE_BTN).isEmpty();
    }

    public boolean waitForEditButtonOnCard(WebElement storyCard) {
        return waitForChildOnCard(storyCard, STORY_EDIT_BTN);
    }

    public boolean waitForDeleteButtonOnCard(WebElement storyCard) {
        return waitForChildOnCard(storyCard, STORY_DELETE_BTN);
    }

    public boolean waitForEditFormOnCard(WebElement storyCard) {
        return waitForChildOnCard(storyCard, STORY_EDIT_FORM);
    }

    public void clickEditOnStory(WebElement storyCard) {
        click(storyCard.findElement(STORY_EDIT_BTN), "Edit button on story card");
    }

    public void clickDeleteOnStory(WebElement storyCard) {
        click(storyCard.findElement(STORY_DELETE_BTN), "Delete button on story card");
    }

    public boolean acceptDeleteConfirmation() {
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
            log.debug("Accepted delete-confirmation alert");
            return true;
        } catch (Exception e) {
            log.warn("No delete-confirmation alert appeared: {}", e.getMessage());
            return false;
        }
    }

    public boolean isEditFormVisibleOnCard(WebElement storyCard) {
        return storyCard != null && !storyCard.findElements(STORY_EDIT_FORM).isEmpty();
    }

    public void saveEditedStory(WebElement storyCard, String newTitle, String newContent) {
        WebElement editForm = storyCard.findElement(STORY_EDIT_FORM);
        clearAndType(editForm.findElement(EDIT_FORM_TITLE_INPUT), newTitle, "Edit story title");
        clearAndType(editForm.findElement(EDIT_FORM_CONTENT_INPUT), newContent, "Edit story content");
        click(editForm.findElement(EDIT_FORM_SAVE_BTN), "Save edited story");
    }

    public boolean isTopContributorsSectionVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(topContributorsHeading)).isDisplayed()
                    && !driver.findElements(CONTRIB_CARDS).isEmpty();
        } catch (Exception e) { return false; }
    }

    public int getTopContributorsCount() {
        return driver.findElements(CONTRIB_CARDS).size();
    }

    public void clickExploreDiscussions() {
        click(exploreDiscussionsBtn, "Explore Discussions");
    }

    private boolean waitForChildOnCard(WebElement storyCard, By childLocator) {
        if (storyCard == null) return false;
        try {
            return wait.until(d -> {
                try {
                    return !storyCard.findElements(childLocator).isEmpty();
                } catch (Exception e) { return false; }
            });
        } catch (Exception e) {
            return false;
        }
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
