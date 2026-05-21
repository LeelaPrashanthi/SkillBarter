package com.cts.mfrp.skillbarter.tests;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.MessengerPage;
import com.cts.mfrp.skillbarter.pages.SignInPage;
import com.cts.mfrp.skillbarter.utils.ConfigReader;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;

/**
 * Test class for the Builtin Messenger.
 *
 * Scenario   : TS_013 - Verify Builtin Messenger Features
 * Requirement: REQ-2.13
 * Test Cases : TC_061 - TC_065
 * Group      : messenger, regression
 */
public class MessengerTest extends BaseTest {

    @SuppressWarnings("unused")
    private MessengerPage messengerPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenMatches() {
        // 1) Show the login page
        navigateToSignIn();

        // 2) Fill the credentials and submit
        new SignInPage(driver).signIn(ConfigReader.getValidEmail(), ConfigReader.getValidPassword());

        // 3) Wait for login to land on the dashboard. Using a dedicated 60s wait
        //    (not the shared 15s `wait` field) because when this class runs LATE
        //    in a full testng.xml pass, the machine is hot and Chrome startup +
        //    login redirect routinely takes > 15s — causing a silent
        //    TimeoutException -> @BeforeMethod fail -> tests reported as
        //    "Skipped". Loose match: accepts /dashboard OR /app/dashboard.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(60))
                    .until(ExpectedConditions.urlContains("dashboard"));
        } catch (Exception timeout) {
            System.out.println("[Messenger] Login wait timed out after 60s. Current URL: " + driver.getCurrentUrl()
                    + " — check that '" + ConfigReader.getValidEmail() + "' credentials are valid.");
            throw timeout;
        }

        // 4) Now go to the Messenger page
        navigateTo(AppConstants.MESSENGER_URL);
        messengerPage = new MessengerPage(driver);
    }


    @Test
    public void tc057_PageTitleValidation(){
        Assert.assertEquals(messengerPage.getPageTitle(), "Messages",
                "Page title is not correct");
    }
//    @Test
//    public void tc087a_SearchBarPresenceOrNot(){
//        if(messengerPage.isSearchBarPresent()){
//            Assert.assertTrue(true,"Search Bar is Present you can serach ");
//            messengerPage.searchForInput("Sai");
//
//
//
////            System.out.println(count);
//        }
//        else{
//            Assert.assertFalse(false,"Search is not present");
//        }
//    }
        @Test
        public void tc58_SearchBarPresenceOrNot() {
            Assert.assertTrue(messengerPage.isSearchBarPresent(), "Search bar not present");

            messengerPage.searchForInput("Sai");

            String value = messengerPage.getSearchBarValue();

            Assert.assertEquals(value, "Sai", "Search input failed");
        }

    @Test
    public void tc59_SearchingValidation() {
        Assert.assertTrue(messengerPage.isSearchBarPresent(), "Search bar not present");

        // Wait up to 30s for actual .contact rows to render (NOT the wrapper div).
        // The earlier xpath matched a positional wrapper that exists before any
        // contact has hydrated, so areConversationsListed() raced ahead and saw 0.
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                        By.cssSelector("app-chat .contacts .contact"), 0));

        Assert.assertTrue(messengerPage.areConversationsListed(), "Conversations were not loaded");

        messengerPage.searchForInput("Sai");

        String value = messengerPage.getSearchBarValue();

        Assert.assertEquals(value, "Sai", "Search input failed");
    }














    /* =====================================================================
     * TC_061 - Conversation list display
     * Pre-condition: User logged in with at least one conversation.
     * Steps:
     *   1. Open Builtin Messenger.
     *   2. Verify list of conversations with avatar, name, message preview, timestamp.
     * Expected: A non-empty list is rendered and every row shows all four fields.
     * =================================================================== */
    @Test(testName = "TC_057", description = "Conversation list shows avatar, name, preview, timestamp",
          groups = {"messenger", "smoke", "regression"}, priority = 61, retryAnalyzer = RetryAnalyzer.class)
    public void tc060_conversationListDisplayed() {
        Assert.assertEquals(messengerPage.getPageTitle(), "Messages",
                "Messenger page title is not 'Messages'");

        Assert.assertTrue(messengerPage.isSearchBarPresent(),
                "Search bar is not present on Messenger page");

        // Give the contact list up to 30s to hydrate before asserting — the
        // .contact <div>s mount before their text fills in, so a single-shot
        // check after navigation can race the render.
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                        By.cssSelector("app-chat .contacts .contact"), 0));

        Assert.assertTrue(messengerPage.areConversationsListed(),
                "No conversations are listed on the Messenger page");
    }

    /* =====================================================================
     * TC_062 - Search conversations
     * Test data: keyword = "William"
     * Steps:
     *   1. Type 'William' in the Search bar.
     *   2. Clear the search.
     * Expected: List filters to entries matching the keyword; clearing restores it.
     * =================================================================== */
    @Test(testName = "TC_058", description = "Search filters conversations by keyword",
          groups = {"messenger", "regression"}, priority = 62, retryAnalyzer = RetryAnalyzer.class)
    public void tc061_searchConversations() {
        // Wait up to 20 seconds until all conversations/contacts are listed in the left panel
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                        By.cssSelector("app-chat .contacts .contact"), 0));



        Assert.assertTrue(messengerPage.isSearchBarPresent(), "Search bar is not present");
        messengerPage.clearSearch();

        final String keyword = "Usha";
        messengerPage.searchForInput(keyword);

        Assert.assertEquals(messengerPage.getSearchBarValue(), keyword,
                "Typed keyword did not register in the search bar");

        int filteredCount = messengerPage.getConversationCount();
        if (filteredCount > 0) {
            Assert.assertTrue(messengerPage.allVisibleConversationsMatch(keyword),
                    "Filtered list contains entries that don't match '" + keyword + "'");
        }
        messengerPage.clearSearch();
    }

    /* =====================================================================
     * TC_063 - Send a message in active chat
     * Test data: message = "Hello"
     * Steps:
     *   1. Open a conversation.
     *   2. Type a message and click Send.
     * Expected: Message appears in the message thread.
     * =================================================================== */
    @Test(testName = "TC_059", description = "Sending a message in an active chat appears in the thread",
          groups = {"messenger", "regression"}, priority = 63, retryAnalyzer = RetryAnalyzer.class)
    public void tc062_sendMessageInActiveChat() {
        // Wait up to 20 seconds until all conversations/contacts are listed in the left panel
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                        By.cssSelector("app-chat .contacts .contact"), 0));

        Assert.assertTrue(messengerPage.areConversationsListed(),
                "No conversation available to open");

        messengerPage.clickFirstConversation();

        // Wait up to 20 seconds until the chat header (contact name h3) is visible
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("app-chat .chat-win .chat-header h3")));

        Assert.assertTrue(messengerPage.isChatWindowOpen(),
                "Chat window did not open after selecting a conversation");
        Assert.assertFalse(messengerPage.getActiveChatName().isEmpty(),
                "Active chat header is missing the contact name");

        final String msg = "Hello";
        messengerPage.typeMessage(msg);

        Assert.assertTrue(messengerPage.isSendButtonEnabled(),
                "Send button stayed disabled after typing a message");

        messengerPage.clickSend();

        Assert.assertTrue(messengerPage.isMessageSent(msg),
                "Sent message '" + msg + "' did not appear in the conversation thread");
    }

    /* =====================================================================
     * TC_064 - Accept session request from chat
     * Pre-condition: Open a chat that has a pending session request (17 June 14:00).
     * Steps:
     *   1. Open a chat with pending session request.
     *   2. Click 'Accept'.
     * Expected: Session request is accepted (panel updates / is removed).
     * =================================================================== */
    @Test(testName = "TC_060", description = "Accept session request from chat",
          groups = {"messenger", "regression"}, priority = 64, retryAnalyzer = RetryAnalyzer.class)
    public void tc063_acceptSessionRequestFromChat() {
        // Test data prerequisite: skip cleanly if the test account has no
        // conversations yet (no fail — this is a data-driven scenario).
        if (!messengerPage.areConversationsListed()) {
            throw new org.testng.SkipException(
                    "No conversation available for this account — cannot test Accept flow.");
        }

        messengerPage.clickFirstConversation();
        Assert.assertTrue(messengerPage.isChatWindowOpen(),
                "Chat window did not open");

        if (!messengerPage.isSessionRequestPanelVisible()) {
            throw new org.testng.SkipException(
                    "No pending session request in the opened chat - cannot validate Accept flow");
        }

        messengerPage.clickAccept();

        Assert.assertTrue(messengerPage.isSessionRequestPanelGone(),
                "Session request panel still visible after clicking Accept");
    }

    /* =====================================================================
     * TC_065 - Decline session request from chat
     * Pre-condition: Open a chat that has a pending session request (17 June 14:00).
     * Steps:
     *   1. Open a chat with pending session request.
     *   2. Click 'Decline'.
     * Expected: Session request is removed from the chat.
     * =================================================================== */
    @Test(testName = "TC_061", description = "Decline session request from chat",
          groups = {"messenger", "regression"}, priority = 65, retryAnalyzer = RetryAnalyzer.class)
    public void tc064_declineSessionRequestFromChat() {
        // Test data prerequisite: skip cleanly if the test account has no
        // conversations yet (no fail — this is a data-driven scenario).
        if (!messengerPage.areConversationsListed()) {
            throw new org.testng.SkipException(
                    "No conversation available for this account — cannot test Decline flow.");
        }

        messengerPage.clickFirstConversation();
        Assert.assertTrue(messengerPage.isChatWindowOpen(),
                "Chat window did not open");

        if (!messengerPage.isSessionRequestPanelVisible()) {
            throw new org.testng.SkipException(
                    "No pending session request in the opened chat - cannot validate Decline flow");
        }

        messengerPage.clickDecline();

        Assert.assertTrue(messengerPage.isSessionRequestPanelGone(),
                "Session request panel still visible after clicking Decline");
    }
}
