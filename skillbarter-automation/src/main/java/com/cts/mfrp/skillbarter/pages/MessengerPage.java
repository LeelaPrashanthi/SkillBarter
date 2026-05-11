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
 * Page Object for the Builtin Messenger page.
 * Covers TC_061 – TC_065 (TS_013).
 */
public class MessengerPage {

    private static final Logger log = LogManager.getLogger(MessengerPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='messenger-title'], [class*='page-title']")
    private WebElement pageTitle;

    @FindBy(css = "input[class*='search'], input[placeholder*='search' i], [class*='search-bar'] input")
    private WebElement searchBar;

    @FindBy(css = "[class*='conversation-list'] [class*='item'], " +
            "[class*='chat-list'] li, [class*='convo-item']")
    private List<WebElement> conversationList;

    @FindBy(css = "[class*='message-input'], textarea[class*='msg'], " +
            "input[placeholder*='message' i]")
    private WebElement messageInput;

    @FindBy(css = "button[class*='send'], [class*='send-btn'], button[type='submit']")
    private WebElement sendBtn;

    @FindBy(css = "[class*='message-bubble'], [class*='chat-bubble'], [class*='msg-item']")
    private List<WebElement> messageBubbles;

    @FindBy(css = "[class*='session-request'], [class*='request-panel']")
    private WebElement sessionRequestPanel;

    @FindBy(css = "button[class*='accept'], [class*='accept-btn']")
    private WebElement acceptBtn;

    @FindBy(css = "button[class*='decline'], [class*='decline-btn']")
    private WebElement declineBtn;

    public MessengerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isSearchBarPresent() {
        return isDisplayed(searchBar);
    }

    public boolean areConversationsListed() {
        return !conversationList.isEmpty();
    }

    public int getConversationCount() {
        return conversationList.size();
    }

    public void searchConversation(String keyword) {
        clearAndType(searchBar, keyword, "Search bar");
    }

    public void clearSearch() {
        try {
            searchBar.clear();
        } catch (Exception e) {
            log.warn("clearSearch failed: {}", e.getMessage());
        }
    }

    public void clickFirstConversation() {
        if (!conversationList.isEmpty()) click(conversationList.get(0), "First conversation");
    }

    public void sendMessage(String msg) {
        clearAndType(messageInput, msg, "Message input");
        click(sendBtn, "Send button");
    }

    public boolean isMessageSent(String msg) {
        return messageBubbles.stream()
                .anyMatch(b -> b.getText().contains(msg));
    }

    public boolean isSessionRequestPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(sessionRequestPanel)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public void clickAccept() {
        click(acceptBtn, "Accept session request");
    }

    public void clickDecline() {
        click(declineBtn, "Decline session request");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

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

    private boolean isDisplayed(WebElement el) {
        try { return el.isDisplayed(); } catch (Exception e) { return false; }
    }
}
