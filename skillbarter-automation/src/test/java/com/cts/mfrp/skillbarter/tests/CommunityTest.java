package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.CommunityPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.UUID;

/**
 * Test class for Community page.
 *
 * Scenario   : TS_016 – Verify Community Page Features
 * Requirement: REQ-2.16
 * Test Cases : TC_074 → TC_089
 * Group      : community, regression
 *
 * Convention: each test has exactly one Assert call. Setup steps rely on
 * WebDriverWait conditions instead — a timeout there fails the test with a
 * TimeoutException, which serves the same purpose as a precondition assert.
 */
public class CommunityTest extends BaseTest {

    private CommunityPage communityPage;

    // Reused locators for inline waits inside the tests below.
    private static final By STORY_FORM = By.xpath("//*[contains(@class,'story-form')]");
    private static final By STORY_EDIT_FORM = By.xpath("//*[contains(@class,'story-edit')]");

    private static By storyTitleLocator(String title) {
        return By.xpath("//*[contains(@class,'stories-grid')]"
                + "//*[contains(@class,'story-card')]"
                + "//h3[normalize-space()='" + title + "']");
    }

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenCommunity() {
        navigateTo(AppConstants.SIGNIN_URL);
        new SignInPage(driver).signIn(AppConstants.VALID_EMAIL, AppConstants.VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("dashboard"));
        navigateTo(AppConstants.COMMUNITY_URL);
        communityPage = new CommunityPage(driver);
    }

    @Test(testName = "TC_079", description = "Community page shows user stories with name, title, content",
          groups = {"community", "regression"}, priority = 1)
    public void tc074_userStoriesDisplayed() {
        Assert.assertTrue(communityPage.areStoriesDisplayed(),
                "No story cards rendered on Community page");
    }

    @Test(testName = "TC_080", description = "Share Your Story opens form and submits story",
          groups = {"community", "regression"}, priority = 75)
    public void tc082_shareYourStory() {
        String uniqueTitle = "Auto Story " + UUID.randomUUID().toString().substring(0, 8);

        communityPage.clickShareYourStory();
        communityPage.submitStory(uniqueTitle, "Shared via automation suite.");

        // Wait for the new story's <h3> to render in the feed.
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(storyTitleLocator(uniqueTitle)));

        Assert.assertTrue(communityPage.storyExistsByTitle(uniqueTitle),
                "Submitted story not found in feed: " + uniqueTitle);
    }

    @Test(testName = "TC_081", description = "Top Contributors section shows user names with XP",
          groups = {"community", "regression"}, priority = 76)
    public void tc083_topContributorsDisplayed() {
        Assert.assertTrue(communityPage.isTopContributorsSectionVisible(),
                "Top Contributors section not visible");
    }

    @Test(testName = "TC_082",
          description = "BUG: Story with only spaces and dots is accepted and posted; expected content-quality validation error",
          groups = {"community", "regression", "bug"}, priority = 78)
    public void tc085_storyWithOnlySpacesAndDotsShouldBeRejected() {
        String junkTitle   = " . . ..";                              // 7 chars, satisfies minlength=5
        String junkContent = " . . . . . . . . . . . . . . . . . ";  // 35 chars, satisfies minlength=20

        communityPage.clickShareYourStory();
        communityPage.submitStory(junkTitle, junkContent);

        // Wait for the submit cycle to finish (form closes on success). This is
        // what exposed the bug originally — without the wait the assertion fired
        // before the buggy POST landed and the test falsely "passed".
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(STORY_FORM));

        Assert.assertFalse(communityPage.storyExistsByTitle(junkTitle),
                "BUG: Junk-title story appears in feed: '" + junkTitle + "'");
    }

    @Test(testName = "TC_083",
          description = "BUG: Story with only numeric input in title and content is accepted; expected content-quality validation error",
          groups = {"community", "regression", "bug"}, priority = 86)
    public void tc086_storyWithOnlyNumbersShouldBeRejected() {
        String numericTitle   = "1234567890";              // 10 chars, satisfies minlength=5
        String numericContent = "98765432109876543210";    // 20 chars, satisfies minlength=20

        communityPage.clickShareYourStory();
        communityPage.submitStory(numericTitle, numericContent);

        // Wait for the submit cycle to finish (form closes on success).
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(STORY_FORM));

        Assert.assertFalse(communityPage.storyExistsByTitle(numericTitle),
                "BUG: All-numeric story appears in feed: '" + numericTitle + "'");
    }

    @Test(testName = "TC_084",
          description = "BUG: Share Your Story form renders two Cancel buttons; expected exactly one",
          groups = {"community", "regression", "bug"}, priority = 87)
    public void tc087_shareStoryFormHasDuplicateCancelButton() {
        communityPage.clickShareYourStory();
        // Wait for the story form to actually render before counting.
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(STORY_FORM));

        Assert.assertEquals(communityPage.getCancelButtonCountInStoryForm(), 1,
                "BUG: Expected exactly 1 Cancel button in the story form");
    }

    @Test(testName = "TC_085",
          description = "Edit option on a user-created story card opens the edit form and saves changes",
          groups = {"community", "regression"}, priority = 88)
    public void tc088_editOwnStoryWorks() {
        String original = "Auto Edit " + UUID.randomUUID().toString().substring(0, 8);
        String updated  = original + " - Updated";

        communityPage.clickShareYourStory();
        communityPage.submitStory(original, "Original story content for edit test.");

        // Wait for the seed story to render.
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(storyTitleLocator(original)));

        WebElement card = communityPage.findStoryCardByTitle(original);
        communityPage.clickEditOnStory(card);

        // Wait for the inline edit form to render on a card.
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(STORY_EDIT_FORM));

        communityPage.saveEditedStory(card, updated, "Edited story content via automation.");

        // Wait for the updated title to appear.
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(storyTitleLocator(updated)));

        Assert.assertTrue(communityPage.storyExistsByTitle(updated),
                "Edited story title not reflected in feed: " + updated);
    }

    @Test(testName = "TC_086",
          description = "Delete option on a user-created story card removes the story from the community feed",
          groups = {"community", "regression"}, priority = 89)
    public void tc089_deleteOwnStoryWorks() {
        String title = "Auto Delete " + UUID.randomUUID().toString().substring(0, 8);

        communityPage.clickShareYourStory();
        communityPage.submitStory(title, "Story content scheduled for deletion.");

        // Wait for the seed story to render.
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(storyTitleLocator(title)));

        WebElement card = communityPage.findStoryCardByTitle(title);
        communityPage.clickDeleteOnStory(card);

        // The app pops a native JS confirm() dialog ("Delete this story?").
        // Wait for the alert, then click OK to accept the deletion.
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.alertIsPresent())
                .accept();

        // Wait for the story's <h3> to actually disappear from the feed.
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.invisibilityOfElementLocated(storyTitleLocator(title)));

        Assert.assertFalse(communityPage.storyExistsByTitle(title),
                "Story still present in feed after delete: " + title);
    }
}
