package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for the Builtin Messenger (Chat) page – /app/chat.
 * Covers TC_061 – TC_065 (TS_013).
 *
 * Layout reference (FRD 2.9):
 *   - Left panel  (.contacts)  → conversation list + search
 *   - Right panel (.chat-win)  → active conversation thread
 */
public class MessengerPage {

    private static final Logger log = LogManager.getLogger(MessengerPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Page header ───────────────────────────────────────────────────────────
    @FindBy(css = "app-chat .contacts .contacts-title")
    private WebElement pageTitle;

    // ── Left panel: search + conversation list ───────────────────────────────
    @FindBy(css = "app-chat .contacts input.si")
    private WebElement searchBar;

    @FindBy(css = "app-chat .contacts .contact")
    private List<WebElement> conversationList;

    @FindBy(css = "app-chat .contacts .contact .cav")
    private List<WebElement> conversationAvatars;

    @FindBy(css = "app-chat .contacts .contact .cname")
    private List<WebElement> conversationNames;

    @FindBy(css = "app-chat .contacts .contact .clast")
    private List<WebElement> conversationLastMessages;

    @FindBy(css = "app-chat .contacts .contact .ctime")
    private List<WebElement> conversationTimestamps;

    // ── Right panel: empty state ──────────────────────────────────────────────
    @FindBy(css = "app-chat .chat-win p")
    private WebElement noChatMessage;

    // ── Right panel: active conversation header ──────────────────────────────
    @FindBy(css = "app-chat .chat-win")
    private WebElement chatWindow;

    @FindBy(css = "app-chat .chat-win .chat-header .back-btn")
    private WebElement backButton;

    @FindBy(css = "app-chat .chat-win .chat-header .chav")
    private WebElement activeChatAvatar;

    @FindBy(css = "app-chat .chat-win .chat-header h3")
    private WebElement activeChatName;

    @FindBy(css = "app-chat .chat-win .chat-header .skill-tag")
    private WebElement activeChatSkillTag;

    // ── Right panel: message thread + composer ───────────────────────────────
    @FindBy(css = "app-chat .chat-win .messages")
    private WebElement messagesContainer;

    @FindBy(css = "app-chat .chat-win .messages .empty-m")
    private WebElement emptyMessagesIndicator;

    // Generic match for any message bubble inside the thread.
    @FindBy(css = "app-chat .chat-win .messages > div:not(.empty-m)")
    private List<WebElement> messageBubbles;

    @FindBy(css = "app-chat .chat-win .input-row .attach-btn")
    private WebElement attachButton;

    @FindBy(css = "app-chat .chat-win .input-row input.mi")
    private WebElement messageInput;

    @FindBy(css = "app-chat .chat-win .input-row button.btn-primary")
    private WebElement sendBtn;

    // ── Session request (TC_064 / TC_065) ────────────────────────────────────
    @FindBy(xpath = "//app-chat//*[contains(@class,'session-request') or contains(@class,'request-panel')]")
    private WebElement sessionRequestPanel;

    @FindBy(xpath = "//app-chat//button[contains(@class,'accept') or contains(translate(normalize-space(.),'ACEPT','acept'),'accept')]")
    private WebElement acceptBtn;

    @FindBy(xpath = "//app-chat//button[contains(@class,'decline') or contains(translate(normalize-space(.),'DECLIN','declin'),'decline')]")
    private WebElement declineBtn;

    public MessengerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Page-level ────────────────────────────────────────────────────────────

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isSearchBarPresent() {
        return isDisplayed(searchBar);
    }

    // ── Conversation list (TC_061) ────────────────────────────────────────────

    public boolean areConversationsListed() {
        return !conversationList.isEmpty();
    }

    public int getConversationCount() {
        return conversationList.size();
    }

    /**
     * Waits until the conversation list has FULLY loaded before returning, by
     * combining three signals:
     *   1. At least one .contact row is visible.
     *   2. document.readyState == 'complete'.
     *   3. Angular reports stable (no pending HTTP requests / timers /
     *      change-detection cycles) via window.getAllAngularTestabilities().
     *
     * Step 3 is the key one: pure DOM-count polling could not tell us whether
     * Angular was still mid-fetch, so the count appeared "stable at 1" before
     * the rest of the conversations were appended.
     *
     * Returns true once the list is loaded; false if the overall wait times out.
     */
    public boolean waitForConversationsToLoad() {
        final By contactLocator = By.xpath(
                "//app-root/app-sidebar/div[1]/div/div/app-chat/div/div[1]/div");
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(60));

            // 1. Wait for the first row to be visible.
            longWait.until(ExpectedConditions.visibilityOfElementLocated(contactLocator));

            // 2. Wait for document.readyState == 'complete'.
            longWait.until(d -> "complete".equals(
                    ((JavascriptExecutor) d)
                            .executeScript("return document.readyState")));

            // 3. Wait for Angular to be stable — i.e. no pending XHRs, timers,
            //    or change-detection runs. Falls back to "true" if the page is
            //    not an Angular app, so this is safe to call unconditionally.
            longWait.until(d -> isAngularStable());

            // 4. Tiny grace period so the *ngFor has flushed the final batch.
           

            int finalCount = driver.findElements(contactLocator).size();
            log.info("Conversation list fully loaded with {} row(s)", finalCount);
            return finalCount > 0;
             } catch (Exception e) {
            log.warn("waitForConversationsToLoad timed out: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns true when Angular reports no outstanding tasks. Uses the
     * standard testability hook exposed on every Angular app
     * (window.getAllAngularTestabilities()). If the hook is missing — e.g.
     * the page hasn't bootstrapped yet or it isn't an Angular app — this
     * returns false so the caller keeps waiting; on script errors it returns
     * true to avoid wedging the wait.
     */
    private boolean isAngularStable() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "try {" +
                    "  if (typeof window.getAllAngularTestabilities !== 'function') return false;" +
                    "  var t = window.getAllAngularTestabilities();" +
                    "  if (!t || t.length === 0) return false;" +
                    "  return t.every(function (x) { return x.isStable(); });" +
                    "} catch (e) { return true; }"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.debug("isAngularStable script failed: {}", e.getMessage());
            return true;
        }
    }

    public List<String> getConversationNames() {
        return conversationNames.stream()
                .map(e -> safeText(e))
                .collect(Collectors.toList());
    }

    public List<String> getConversationLastMessages() {
        return conversationLastMessages.stream()
                .map(e -> safeText(e))
                .collect(Collectors.toList());
    }

    public List<String> getConversationTimestamps() {
        return conversationTimestamps.stream()
                .map(e -> safeText(e))
                .collect(Collectors.toList());
    }

    /**
     * Each conversation row must show: avatar, name, last-message preview, timestamp.
     * Returns true only when every row has all four fields populated.
     */
    public boolean allConversationsHaveAllFields() {
        int count = conversationList.size();
        if (count == 0) return false;
        if (conversationAvatars.size() < count) return false;
        if (conversationNames.size() < count) return false;
        if (conversationLastMessages.size() < count) return false;
        if (conversationTimestamps.size() < count) return false;
        for (int i = 0; i < count; i++) {
            if (!isDisplayed(conversationAvatars.get(i))) return false;
            if (safeText(conversationNames.get(i)).isEmpty()) return false;
            if (safeText(conversationLastMessages.get(i)).isEmpty()) return false;
            if (safeText(conversationTimestamps.get(i)).isEmpty()) return false;
        }
        return true;
    }

    public String getNoChatMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(noChatMessage)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    // ── Search (TC_062) ───────────────────────────────────────────────────────

    public void searchConversation(String keyword) {
        clearAndType(searchBar, keyword, "Search bar");
    }

    public void clearSearch() {
        try {
            wait.until(ExpectedConditions.visibilityOf(searchBar));
            searchBar.sendKeys(Keys.CONTROL, "a");
            searchBar.sendKeys(Keys.DELETE);
            // Fallback in case the above key combo isn't honoured.
            if (!searchBar.getAttribute("value").isEmpty()) {
                searchBar.clear();
            }
        } catch (Exception e) {
            log.warn("clearSearch failed: {}", e.getMessage());
        }
    }

    public String getSearchBarValue() {
        return searchBar.getAttribute("value");
    }

    /** Returns true if every visible conversation name contains the keyword (case-insensitive). */
    public boolean allVisibleConversationsMatch(String keyword) {
        List<String> names = getConversationNames();
        if (names.isEmpty()) return false;
        String k = keyword.toLowerCase();
        return names.stream().allMatch(n -> n.toLowerCase().contains(k));
    }

    // ── Conversation selection (TC_063 – TC_065) ─────────────────────────────

    public void clickFirstConversation() {
        if (!conversationList.isEmpty()) click(conversationList.get(0), "First conversation");
    }

    public void clickConversationByName(String name) {
        for (int i = 0; i < conversationNames.size(); i++) {
            if (safeText(conversationNames.get(i)).equalsIgnoreCase(name)) {
                click(conversationList.get(i), "Conversation: " + name);
                return;
            }
        }
        log.warn("Conversation '{}' not found in list", name);
    }

    public boolean isChatWindowOpen() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(chatWindow)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public String getActiveChatName() {
        try { return wait.until(ExpectedConditions.visibilityOf(activeChatName)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getActiveChatSkillTag() {
        try { return wait.until(ExpectedConditions.visibilityOf(activeChatSkillTag)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getEmptyMessagesText() {
        try { return emptyMessagesIndicator.getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public boolean isSendButtonEnabled() {
        try { return sendBtn.isEnabled(); } catch (Exception e) { return false; }
    }

    // ── Send a message (TC_063) ───────────────────────────────────────────────

    public void typeMessage(String msg) {
        clearAndType(messageInput, msg, "Message input");
    }

    public void clickSend() {
        click(sendBtn, "Send button");
    }

    public void sendMessage(String msg) {
        typeMessage(msg);
        clickSend();
    }

    public boolean isMessageSent(String msg) {
        try {
            return wait.until(d -> messageBubbles.stream()
                    .anyMatch(b -> safeText(b).contains(msg)));
        } catch (Exception e) {
            // Fallback: any descendant whose text contains the message.
            try {
                List<WebElement> matches = messagesContainer.findElements(
                        By.xpath(".//*[contains(normalize-space(.), '" + msg + "')]"));
                return !matches.isEmpty();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public int getMessageCount() {
        return messageBubbles.size();
    }

    // ── Session request (TC_064 / TC_065) ────────────────────────────────────

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

    public boolean isSessionRequestPanelGone() {
        try {
            wait.until(ExpectedConditions.invisibilityOf(sessionRequestPanel));
            return true;
        } catch (Exception e) { return false; }
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

    private String safeText(WebElement el) {
        try { return el.getText().trim(); } catch (Exception e) { return ""; }
    }

    public void searchForInput(String input) {
        try {
            searchBar.click();
            searchBar.sendKeys(input);
            log.info("Typed '{}' in search bar", input);
        } catch (Exception e) {
            log.error("Typing failed: {}", e.getMessage());
            throw e;
        }
    }
}
