package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.CommunityPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Test class for Community page.
 *
 * Scenario   : TS_016 – Verify Community Page Features
 * Requirement: REQ-2.16
 * Test Cases : TC_074 → TC_082
 * Group      : community, regression
 */
public class CommunityTest extends BaseTest {

    private CommunityPage communityPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenCommunity() {
        navigateTo(AppConstants.SIGNIN_URL);
        new SignInPage(driver).signIn(AppConstants.VALID_EMAIL, AppConstants.VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("dashboard"));
        navigateTo(AppConstants.COMMUNITY_URL);
        communityPage = new CommunityPage(driver);
    }

//    @Test(testName = "TC_074", description = "Community page shows user stories with name, title, content",
//          groups = {"community", "regression"}, priority = 1, retryAnalyzer = RetryAnalyzer.class)
//    public void tc074_userStoriesDisplayed() {
//        Assert.assertEquals(communityPage.getPageTitle(), AppConstants.COMMUNITY_TITLE,
//                "Community page heading mismatch");
//        Assert.assertTrue(communityPage.areStoriesDisplayed(), "No story cards rendered");
//        Assert.assertFalse(communityPage.getFirstStoryAuthor().isEmpty(), "Story author missing");
//        Assert.assertFalse(communityPage.getFirstStoryTitle().isEmpty(), "Story title missing");
//        Assert.assertFalse(communityPage.getFirstStoryContent().isEmpty(), "Story content missing");
//    }

    @Test(testName = "TC_075", description = "Share Your Story opens form and submits story",
          groups = {"community", "regression"}, priority = 75, retryAnalyzer = RetryAnalyzer.class)
    public void tc075_shareYourStory() {
        String uniqueTitle = "Auto Story " + UUID.randomUUID().toString().substring(0, 8);

        communityPage.clickShareYourStory();
        communityPage.submitStory(uniqueTitle, "Shared via automation suite.");

        Assert.assertTrue(communityPage.storyExistsByTitle(uniqueTitle),
                "Submitted story not found in feed: " + uniqueTitle);
    }

    @Test(testName = "TC_076", description = "Top Contributors section shows user names with XP",
          groups = {"community", "regression"}, priority = 76, retryAnalyzer = RetryAnalyzer.class)
    public void tc076_topContributorsDisplayed() {
        Assert.assertTrue(communityPage.isTopContributorsSectionVisible(),
                "Top Contributors section not visible");
        Assert.assertTrue(communityPage.getTopContributorsCount() > 0,
                "No contributor cards rendered");
    }

    @Test(testName = "TC_077", description = "Explore Community Discussions navigates to forum",
          groups = {"community", "regression"}, priority = 77, retryAnalyzer = RetryAnalyzer.class)
    public void tc077_exploreCommunityDiscussions() {
        communityPage.clickExploreDiscussions();
        String url = getCurrentUrl().toLowerCase();
        Assert.assertTrue(url.contains("discussion") || url.contains("forum"),
                "Did not navigate to discussions/forum. URL: " + url);
    }

    @Test(testName = "TC_078",
          description = "BUG: Story with only spaces and dots is accepted and posted; expected content-quality validation error",
          groups = {"community", "regression", "bug"}, priority = 78, retryAnalyzer = RetryAnalyzer.class)
    public void tc078_storyWithOnlySpacesAndDotsShouldBeRejected() {
        String junkTitle   = " . . ..";                              // 7 chars, satisfies minlength=5
        String junkContent = " . . . . . . . . . . . . . . . . . ";  // 35 chars, satisfies minlength=20
        int before = communityPage.getStoryCount();

        communityPage.clickShareYourStory();
        Assert.assertTrue(communityPage.isStoryFormVisible(), "Story form did not open");
        communityPage.submitStory(junkTitle, junkContent);

        Assert.assertEquals(communityPage.getStoryCount(), before,
                "BUG: Story with only spaces and dots was accepted. Expected content-quality validation to block submission.");
        Assert.assertFalse(communityPage.storyExistsByTitle(junkTitle),
                "BUG: Junk-title story appears in feed: '" + junkTitle + "'");
    }

    @Test(testName = "TC_079",
          description = "BUG: Story with only numeric input in title and content is accepted; expected content-quality validation error",
          groups = {"community", "regression", "bug"}, priority = 79, retryAnalyzer = RetryAnalyzer.class)
    public void tc079_storyWithOnlyNumbersShouldBeRejected() {
        String numericTitle   = "1234567890";              // 10 chars, satisfies minlength=5
        String numericContent = "98765432109876543210";    // 20 chars, satisfies minlength=20
        int before = communityPage.getStoryCount();

        communityPage.clickShareYourStory();
        Assert.assertTrue(communityPage.isStoryFormVisible(), "Story form did not open");
        communityPage.submitStory(numericTitle, numericContent);

        Assert.assertEquals(communityPage.getStoryCount(), before,
                "BUG: Story with only numeric title/content was accepted. Expected content-quality validation to block submission.");
        Assert.assertFalse(communityPage.storyExistsByTitle(numericTitle),
                "BUG: All-numeric story appears in feed: '" + numericTitle + "'");
    }

    @Test(testName = "TC_080",
          description = "BUG: Share Your Story form renders two Cancel buttons; expected exactly one",
          groups = {"community", "regression", "bug"}, priority = 80, retryAnalyzer = RetryAnalyzer.class)
    public void tc080_shareStoryFormHasDuplicateCancelButton() {
        communityPage.clickShareYourStory();
        Assert.assertTrue(communityPage.isStoryFormVisible(), "Story form did not open");

        int cancelCount = communityPage.getCancelButtonCountInStoryForm();
        Assert.assertEquals(cancelCount, 1,
                "BUG: Expected exactly 1 Cancel button in the story form but found " + cancelCount);
    }

    @Test(testName = "TC_081",
          description = "Edit option on a user-created story card opens the edit form and saves changes",
          groups = {"community", "regression"}, priority = 81, retryAnalyzer = RetryAnalyzer.class)
    public void tc081_editOwnStoryWorks() {
        String original = "Auto Edit " + UUID.randomUUID().toString().substring(0, 8);
        String updated  = original + " - Updated";

        communityPage.clickShareYourStory();
        communityPage.submitStory(original, "Original story content for edit test.");
        Assert.assertTrue(communityPage.storyExistsByTitle(original),
                "Pre-condition failed: seed story not posted");

        WebElement card = communityPage.findStoryCardByTitle(original);
        Assert.assertNotNull(card, "Newly created story card not found");
        Assert.assertTrue(communityPage.isEditButtonPresent(card),
                "Edit button missing on user's own story card");

        communityPage.clickEditOnStory(card);
        Assert.assertTrue(communityPage.isEditFormVisibleOnCard(card),
                "Edit form did not open on the story card");

        communityPage.saveEditedStory(card, updated, "Edited story content via automation.");
        Assert.assertTrue(communityPage.storyExistsByTitle(updated),
                "Edited story title not reflected in feed: " + updated);
    }

    @Test(testName = "TC_082",
          description = "Delete option on a user-created story card removes the story from the community feed",
          groups = {"community", "regression"}, priority = 82, retryAnalyzer = RetryAnalyzer.class)
    public void tc082_deleteOwnStoryWorks() {
        String title = "Auto Delete " + UUID.randomUUID().toString().substring(0, 8);

        communityPage.clickShareYourStory();
        communityPage.submitStory(title, "Story content scheduled for deletion.");
        Assert.assertTrue(communityPage.storyExistsByTitle(title),
                "Pre-condition failed: seed story not posted");

        WebElement card = communityPage.findStoryCardByTitle(title);
        Assert.assertNotNull(card, "Newly created story card not found");
        Assert.assertTrue(communityPage.isDeleteButtonPresent(card),
                "Delete button missing on user's own story card");

        int before = communityPage.getStoryCount();
        communityPage.clickDeleteOnStory(card);

        Assert.assertFalse(communityPage.storyExistsByTitle(title),
                "Story still present in feed after delete: " + title);
        Assert.assertTrue(communityPage.getStoryCount() < before,
                "Story count did not decrease after delete");
    }
}
