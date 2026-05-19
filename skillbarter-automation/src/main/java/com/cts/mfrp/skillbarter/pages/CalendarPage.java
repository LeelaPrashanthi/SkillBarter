package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Calendar & Sessions page (/app/calendar).
 * Covers TC_066 – TC_074 (TS_014).
 *
 * Locators are anchored on visible text labels to stay robust against
 * Angular class-hash churn between builds.
 */
public class CalendarPage {

    private static final Logger log = LogManager.getLogger(CalendarPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Calendar grid ────────────────────────────────────────────────────────
    @FindBy(xpath = "//*[normalize-space()='+ New Session Request' " +
            "or normalize-space()='+New Session Request' " +
            "or (self::button and contains(.,'New Session Request')) " +
            "or (self::a and contains(.,'New Session Request'))]")
    private WebElement newSessionRequestBtn;

    @FindBy(xpath = "//*[normalize-space()='Su']")
    private List<WebElement> dayOfWeekHeaders;

    // ── Tabs (scoped to the .stabs container inside .sess-card) ────────────
    @FindBy(xpath = "//div[contains(@class,'stabs')]//button[normalize-space()='Upcoming']")
    private WebElement upcomingTab;

    @FindBy(xpath = "//div[contains(@class,'stabs')]//button[normalize-space()='History']")
    private WebElement historyTab;

    @FindBy(xpath = "//div[contains(@class,'stabs')]//button[contains(@class,'active')]")
    private WebElement activeTab;

    // ── Session items (one per card) ────────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'sitem')]")
    private List<WebElement> sessionItems;

    // Actions are scoped inside .sitem so we don't match unrelated buttons elsewhere.
    @FindBy(xpath = "//div[contains(@class,'sitem')]//button[contains(.,'Join Call')] " +
            "| //div[contains(@class,'sitem')]//a[contains(.,'Join Call')]")
    private List<WebElement> joinCallBtns;

    @FindBy(xpath = "//div[contains(@class,'sitem')]//button[contains(.,'Mark as Complete')] " +
            "| //div[contains(@class,'sitem')]//a[contains(.,'Mark as Complete')]")
    private List<WebElement> markCompleteBtns;

    @FindBy(xpath = "//div[contains(@class,'sitem')]//button[normalize-space()='Rate'] " +
            "| //div[contains(@class,'sitem')]//a[normalize-space()='Rate']")
    private List<WebElement> rateBtns;

    @FindBy(xpath = "//div[contains(@class,'sitem')]//*[normalize-space()='Scheduled']")
    private List<WebElement> scheduledBadges;

    @FindBy(xpath = "//div[contains(@class,'sitem')]//*[normalize-space()='Completed']")
    private List<WebElement> completedBadges;

    // ── Toast / inline confirmation after a successful create ──────────────
    @FindBy(xpath = "//*[contains(translate(., 'CREATED', 'created'), 'created successfully') " +
            "or contains(., 'Session request created')]")
    private WebElement successMessage;

    // ── Create Session Request form ──────────────────────────────────────────
    @FindBy(xpath = "//*[normalize-space()='Create Session Request']")
    private WebElement createFormHeading;

    // Position-based: Match is the 1st select, Skill the 2nd inside .req-grid.
    // Anchoring on text inside <option> proved unreliable because Angular may
    // render the "Select match" placeholder via styling rather than a real <option>.
    @FindBy(xpath = "(//div[contains(@class,'req-grid')]//select)[1]")
    private WebElement matchSelect;

    @FindBy(xpath = "(//div[contains(@class,'req-grid')]//select)[2]")
    private WebElement skillSelect;

    @FindBy(xpath = "//div[contains(@class,'req-grid')]//input[@type='datetime-local']")
    private WebElement dateTimeInput;

    // Anchor on both the class AND the text. The three buttons inside .req-actions
    // are .btn-primary (Create Request) / .btn-secondary (Reset) / .btn-ghost (Cancel) —
    // class + text together makes the locator unambiguous.
    @FindBy(xpath = "//div[contains(@class,'req-actions')]" +
            "//button[contains(@class,'btn-primary') and normalize-space()='Create Request']")
    private WebElement createRequestBtn;

    @FindBy(xpath = "//div[contains(@class,'req-actions')]" +
            "//button[contains(@class,'btn-secondary') and normalize-space()='Reset']")
    private WebElement resetBtn;

    @FindBy(xpath = "//div[contains(@class,'req-actions')]" +
            "//button[contains(@class,'btn-ghost') and normalize-space()='Cancel']")
    private WebElement cancelBtn;

    public CalendarPage(WebDriver driver) {
        this.driver = driver;
        // Bumped from 15s → 40s. The calendar page can take 20+ seconds to
        // hydrate on slow builds, so clicks via the click() helper need a
        // longer clickability budget — otherwise tab clicks silently fail
        // and the tests skip on an empty session list.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        PageFactory.initElements(driver, this);
    }

    // ── Page readiness ──────────────────────────────────────────────────────

    /**
     * Calendar hydrates over ~6–7 seconds. Polls for the New Session Request
     * button as the readiness indicator. Returns true once ready, false on
     * timeout.
     */
    public boolean waitForPageReady(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(ExpectedConditions.visibilityOf(newSessionRequestBtn));
            return true;
        } catch (Exception e) {
            log.warn("Calendar page didn't become ready within {}s", timeoutSeconds);
            return false;
        }
    }

    /**
     * Session cards (.sitem) are populated by an async fetch that can take up
     * to ~20s on slow builds — separate from the page-shell readiness covered
     * by {@link #waitForPageReady(int)}. Call this after switching tabs
     * (Upcoming/History) and before asserting on counts or action buttons,
     * so TC_072/073/074 don't read an empty list and skip themselves.
     *
     * Bypasses the global 10s implicit wait while polling, otherwise each
     * "is the list empty?" probe would block for 10s and the loop would
     * only get ~3 chances inside a 30s budget. Implicit wait is restored
     * before returning.
     *
     * Returns true if at least one session card appeared, false on timeout.
     */
    public boolean waitForSessionsLoaded(int timeoutSeconds) {
        org.openqa.selenium.By sitemBy =
                org.openqa.selenium.By.xpath("//div[contains(@class,'sitem')]");
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(0));
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(Exception.class)
                    .until(d -> !d.findElements(sitemBy).isEmpty());
            return true;
        } catch (Exception e) {
            log.warn("No session cards appeared within {}s on the active tab", timeoutSeconds);
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(com.cts.mfrp.skillbarter.constants.AppConstants.IMPLICIT_WAIT));
        }
    }

    public boolean isCalendarGridVisible() {
        return !dayOfWeekHeaders.isEmpty();
    }

    public boolean isNewSessionRequestBtnVisible() {
        try { return newSessionRequestBtn.isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public int getDayOfWeekHeaderCount() { return dayOfWeekHeaders.size(); }

    // ── Tabs ────────────────────────────────────────────────────────────────

    public void clickUpcomingTab() { click(upcomingTab, "Upcoming tab"); }
    public void clickHistoryTab()  { click(historyTab, "History tab"); }

    /**
     * Count of session cards on the currently-active tab. Each card is a
     * &lt;div class="sitem"&gt;. Switch tabs (clickUpcomingTab / clickHistoryTab)
     * before calling for tab-specific counts.
     */
    public int getSessionCount() { return sessionItems.size(); }

    public int getUpcomingSessionCount() { return getSessionCount(); }
    public int getHistorySessionCount()  { return getSessionCount(); }

    /** Returns the visible text of the currently-active tab, e.g. "Upcoming". */
    public String getActiveTabText() {
        try { return activeTab.getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public boolean isSuccessMessageVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(successMessage)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public int getScheduledBadgeCount()  { return scheduledBadges.size(); }
    public int getCompletedBadgeCount()  { return completedBadges.size(); }

    public boolean hasJoinCallButton()       { return !joinCallBtns.isEmpty(); }
    public boolean hasMarkCompleteButton()   { return !markCompleteBtns.isEmpty(); }

    public void clickFirstJoinCall() {
        if (!joinCallBtns.isEmpty()) click(joinCallBtns.get(0), "Join Call");
    }

    public void clickFirstMarkComplete() {
        if (!markCompleteBtns.isEmpty()) click(markCompleteBtns.get(0), "Mark as Complete");
    }

    // ── Form ────────────────────────────────────────────────────────────────

    public void clickNewSessionRequest() { click(newSessionRequestBtn, "+ New Session Request"); }

    public boolean isFormVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(createFormHeading)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    /**
     * Selects an option in the Match dropdown by visible-text-position (1-based,
     * skipping the placeholder "Select match" at index 0). Returns the visible
     * text actually selected, or empty string if selection failed.
     */
    public String selectMatchByOptionIndex(int oneBasedIndex) {
        return selectByIndex(matchSelect, oneBasedIndex, "Match");
    }

    public String selectSkillByOptionIndex(int oneBasedIndex) {
        return selectByIndex(skillSelect, oneBasedIndex, "Skill");
    }

    public int getMatchOptionCount() {
        try { return new Select(matchSelect).getOptions().size(); }
        catch (Exception e) { return 0; }
    }

    public int getSkillOptionCount() {
        try { return new Select(skillSelect).getOptions().size(); }
        catch (Exception e) { return 0; }
    }

    /**
     * Skill &lt;select&gt; starts with the {@code disabled} attribute AND empty.
     * After a Match is picked, Angular (a) removes the disabled attribute, then
     * (b) fetches and renders the Skill options. This wait polls for BOTH to
     * complete — not just enable — so callers can immediately select an option
     * without race conditions. Call between selectMatchByOptionIndex() and
     * selectSkillByOptionIndex().
     */
    public boolean waitForSkillEnabled(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(250))
                    .until(d -> {
                        try {
                            return skillSelect.isEnabled()
                                && new Select(skillSelect).getOptions().size() > 1;
                        } catch (Exception inner) {
                            return false;
                        }
                    });
            return true;
        } catch (Exception e) {
            log.warn("Skill select did not become ready (enabled + options populated) within {}s",
                    timeoutSeconds);
            return false;
        }
    }

    /**
     * Types into the datetime input. For an input[type=datetime-local] the
     * expected format is "yyyy-MM-ddTHH:mm" (e.g. "2026-05-25T14:30").
     */
    public void enterDateTime(String isoDateTime) {
        try {
            wait.until(ExpectedConditions.visibilityOf(dateTimeInput));
            dateTimeInput.click();
            dateTimeInput.clear();
            dateTimeInput.sendKeys(isoDateTime);
        } catch (Exception e) {
            log.warn("enterDateTime failed: {}", e.getMessage());
        }
    }

    public boolean isCreateRequestEnabled() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(createRequestBtn)).isEnabled();
        } catch (Exception e) { return false; }
    }

    public void clickCreateRequest() { click(createRequestBtn, "Create Request"); }
    public void clickReset()         { click(resetBtn, "Reset"); }
    public void clickCancel()        { click(cancelBtn, "Cancel"); }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String selectByIndex(WebElement selectEl, int oneBasedIndex, String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(selectEl));
            Select s = new Select(selectEl);
            if (oneBasedIndex < s.getOptions().size()) {
                s.selectByIndex(oneBasedIndex);
                String txt = s.getFirstSelectedOption().getText().trim();
                log.debug("{} selected: {}", name, txt);
                return txt;
            }
            log.warn("{} dropdown only has {} options, can't pick index {}",
                    name, s.getOptions().size(), oneBasedIndex);
        } catch (Exception e) {
            log.warn("{} selection failed: {}", name, e.getMessage());
        }
        return "";
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
