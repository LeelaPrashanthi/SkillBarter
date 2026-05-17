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
 * Page Object for the Matches page (app-matches).
 *
 * Covers TC_056 – TC_060 (TS_012).
 *
 * Locator strategy: all locators are XPath because the Matches page renders
 * cards dynamically (Find New Matches → .ucard list, My Matches → .mcard
 * list, or a .hint empty-state when nothing has been searched yet). XPath
 * lets us anchor by visible text (tab labels, button text, card name) which
 * is more stable than auto-generated _ngcontent attributes.
 */
public class MatchesPage {

    private static final Logger log = LogManager.getLogger(MatchesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Top bar ──────────────────────────────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'sp-badge')]//span[contains(@class,'sp-value')]")
    private WebElement spValue;

    @FindBy(xpath = "//button[contains(@class,'theme-btn')]")
    private WebElement themeToggleBtn;

    @FindBy(xpath = "//button[contains(@class,'icon-btn') and not(contains(@class,'theme-btn'))]")
    private WebElement notificationBellBtn;

    @FindBy(xpath = "//div[contains(@class,'user-btn')]//span[contains(@class,'user-name')]")
    private WebElement userNameLabel;

    @FindBy(xpath = "//div[contains(@class,'user-btn')]")
    private WebElement userDropdownBtn;

    // ── Tabs ─────────────────────────────────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'tabs')]/button[normalize-space()='Find New Matches']")
    private WebElement findNewMatchesTab;

    @FindBy(xpath = "//div[contains(@class,'tabs')]/button[normalize-space()='My Matches']")
    private WebElement myMatchesTab;

    @FindBy(xpath = "//div[contains(@class,'tabs')]/button[contains(@class,'active')]")
    private WebElement activeTab;

    // ── Filters (only render on "Find New Matches") ──────────────────────────
    @FindBy(xpath = "//div[contains(@class,'filters')]//input[contains(@class,'search-input')]")
    private WebElement searchInput;

    @FindBy(xpath = "//div[contains(@class,'filters')]//button[normalize-space()='Search']")
    private WebElement searchBtn;

    // ── Empty / hint state ───────────────────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'hint')]")
    private WebElement hintBox;

    @FindBy(xpath = "//div[contains(@class,'hint')]//p")
    private WebElement hintMessage;

    @FindBy(xpath = "//div[contains(@class,'hint')]//button[normalize-space()='Browse All Users']")
    private WebElement browseAllUsersBtn;

    // ── Find New Matches grid (.ucard) ───────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'grid')]/div[contains(@class,'ucard')]")
    private List<WebElement> userCards;

    @FindBy(xpath = "//div[contains(@class,'ucard')][1]//button[contains(@class,'btn-primary')]")
    private WebElement firstConnectBtn;

    // ── My Matches grid (.mcard) ─────────────────────────────────────────────
    @FindBy(xpath = "//div[contains(@class,'grid')]/div[contains(@class,'mcard')]")
    private List<WebElement> myMatchCards;

    @FindBy(xpath = "//div[contains(@class,'mcard')][1]//a[normalize-space()='Message']")
    private WebElement firstMessageLink;

    public MatchesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Top bar ──────────────────────────────────────────────────────────────

    public String getSpValue() {
        return safeText(spValue);
    }

    public String getLoggedInUserName() {
        return safeText(userNameLabel);
    }

    public void clickThemeToggle() {
        click(themeToggleBtn, "Theme toggle");
    }

    public void clickNotificationBell() {
        click(notificationBellBtn, "Notification bell");
    }

    public void openUserDropdown() {
        click(userDropdownBtn, "User dropdown");
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    public void clickFindNewMatches() {
        click(findNewMatchesTab, "Find New Matches tab");
    }

    public void clickMyMatches() {
        click(myMatchesTab, "My Matches tab");
    }

    public boolean isFindNewMatchesTabVisible() {
        return isDisplayed(findNewMatchesTab);
    }

    public boolean isMyMatchesTabVisible() {
        return isDisplayed(myMatchesTab);
    }

    public String getActiveTabName() {
        return safeText(activeTab);
    }

    public boolean isFindNewMatchesActive() {
        return "Find New Matches".equalsIgnoreCase(getActiveTabName());
    }

    public boolean isMyMatchesActive() {
        return "My Matches".equalsIgnoreCase(getActiveTabName());
    }

    // ── Search ───────────────────────────────────────────────────────────────

    public void searchUser(String name) {
        try {
            wait.until(ExpectedConditions.visibilityOf(searchInput));
            searchInput.clear();
            searchInput.sendKeys(name);
            // Search button is disabled until input has >=2 chars (minlength="2")
            wait.until(ExpectedConditions.elementToBeClickable(searchBtn)).click();
            log.debug("Searched for user: {}", name);
        } catch (Exception e) {
            log.warn("searchUser failed for '{}': {}", name, e.getMessage());
        }
    }

    public boolean isSearchButtonEnabled() {
        try {
            return searchBtn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Empty / hint state ───────────────────────────────────────────────────

    public boolean isHintVisible() {
        return isDisplayed(hintBox);
    }

    public String getHintMessage() {
        return safeText(hintMessage);
    }

    public void clickBrowseAllUsers() {
        click(browseAllUsersBtn, "Browse All Users");
    }

    // ── Find New Matches grid (.ucard) ───────────────────────────────────────

    /**
     * Wait until at least one user card is rendered. Returns false if the
     * page only shows the hint/empty state. Uses a 30s wait because the
     * matches grid can lag on a cold backend.
     */
    public boolean waitForUserCardsToLoad() {
        By cardLocator = By.xpath("//div[contains(@class,'ucard')]");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(35))
                    .until(ExpectedConditions.visibilityOfElementLocated(cardLocator));
            return !driver.findElements(cardLocator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int getUserCardCount() {
        return userCards.size();
    }

    public boolean areUserCardsDisplayed() {
        return !userCards.isEmpty() && userCards.get(0).isDisplayed();
    }

    /** Names of all rendered .ucard h3 entries, in display order. */
    public List<String> getUserCardNames() {
        return driver.findElements(By.xpath(
                "//div[contains(@class,'ucard')]/h3"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }

    /**
     * Dynamic card lookup by visible name. Anchors at the .ucard whose
     * <h3> text equals the supplied name, then walks back up to the card.
     */
    private WebElement userCardByName(String name) {
        return driver.findElement(By.xpath(
                "//div[contains(@class,'ucard')][.//h3[normalize-space()='" + name + "']]"));
    }

    public String getEmailFor(String name) {
        return safeText(userCardByName(name)
                .findElement(By.xpath(".//p[contains(@class,'uemail')]")));
    }

    public String getBioFor(String name) {
        try {
            return userCardByName(name)
                    .findElement(By.xpath(".//p[contains(@class,'ubio')]"))
                    .getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String getMatchScoreFor(String name) {
        return safeText(userCardByName(name)
                .findElement(By.xpath(".//div[contains(@class,'mscore')]")));
    }

    /** Click "Connect" on the .ucard for the user with the given name. */
    public void clickConnectFor(String name) {
        try {
            WebElement btn = userCardByName(name).findElement(By.xpath(
                    ".//button[contains(@class,'btn-primary') and normalize-space()='Connect']"));
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
            log.debug("Clicked Connect for: {}", name);
        } catch (Exception e) {
            log.warn("clickConnectFor failed for '{}': {}", name, e.getMessage());
        }
    }

    public boolean isConnectedFor(String name) {
        try {
            return !userCardByName(name).findElements(By.xpath(
                    ".//button[normalize-space()='Connected' and @disabled]")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if a .ucard with the given name is currently rendered in Find New Matches. */
    public boolean hasUserCardFor(String name) {
        return !driver.findElements(By.xpath(
                "//div[contains(@class,'ucard')][.//h3[normalize-space()='" + name + "']]"))
                .isEmpty();
    }

    /** Visible text of the Connect/Connected button on the card for the given user. */
    public String getConnectButtonTextFor(String name) {
        try {
            return userCardByName(name).findElement(By.xpath(
                    ".//button[contains(@class,'btn-primary')]")).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** True if the Connect/Connected button on the card is HTML-disabled. */
    public boolean isConnectButtonDisabledFor(String name) {
        try {
            WebElement btn = userCardByName(name).findElement(By.xpath(
                    ".//button[contains(@class,'btn-primary')]"));
            return !btn.isEnabled() || btn.getAttribute("disabled") != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * True if Selenium considers the Connect button clickable (visible + enabled).
     * Used to assert the negative — once Connected, the button must NOT be clickable.
     */
    public boolean isConnectButtonClickableFor(String name) {
        try {
            WebElement btn = userCardByName(name).findElement(By.xpath(
                    ".//button[contains(@class,'btn-primary')]"));
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(btn));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** CSS class string of the Connect/Connected button — used to detect a color/state change. */
    public String getConnectButtonClassFor(String name) {
        try {
            return userCardByName(name).findElement(By.xpath(
                    ".//button[contains(@class,'btn-primary')]"))
                    .getAttribute("class");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Poll up to {@code timeoutSeconds} for the Connect button on the .ucard
     * for {@code name} to flip to the "Connected" state (text == "Connected"
     * AND the button is disabled). Returns true on first success, false on
     * timeout.
     *
     * The Find New Matches grid is a one-shot suggestions API — toggling
     * tabs only shows the cached list, so we hard-refresh the page every
     * ~10s (driver.navigate().refresh()) to force Angular to re-fetch.
     * After each refresh we re-click Find New Matches and wait for the
     * grid to repaint before re-checking the button state.
     */
    public boolean waitForConnectButtonToShowConnected(String name, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        long lastRefresh = 0L; // 0 → force a refresh on the first iteration
        while (System.currentTimeMillis() < deadline) {
            try {
                if ("Connected".equalsIgnoreCase(getConnectButtonTextFor(name))
                        && isConnectButtonDisabledFor(name)) {
                    return true;
                }
            } catch (Exception ignored) {
                // Stale element during repaint — retry on next iteration.
            }
            if (System.currentTimeMillis() - lastRefresh > 10_000L) {
                try {
                    driver.navigate().refresh();
                    new WebDriverWait(driver, Duration.ofSeconds(15))
                            .until(ExpectedConditions.elementToBeClickable(findNewMatchesTab));
                    findNewMatchesTab.click();
                    new WebDriverWait(driver, Duration.ofSeconds(15))
                            .until(ExpectedConditions.visibilityOfElementLocated(
                                    By.xpath("//div[contains(@class,'ucard')]")));
                    log.debug("Hard-refreshed page to force Find New Matches re-fetch while waiting for '{}' to flip to Connected", name);
                } catch (Exception ignored) {
                    // Refresh / tab / cards might not settle in 15s on this iteration — keep polling.
                }
                lastRefresh = System.currentTimeMillis();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /** True if the user card exposes a Connect (or Connected) button. */
    public boolean hasConnectButtonFor(String name) {
        try {
            return !userCardByName(name).findElements(By.xpath(
                    ".//button[contains(@class,'btn-primary') and " +
                    "(normalize-space()='Connect' or normalize-space()='Connected')]")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Permissive XPath for "anything that looks like an avatar inside a card".
     * Cards in this app render either an uploaded image (img/svg) OR a styled
     * letter-circle (a div with classes like 'avatar', 'initial', 'profile-pic',
     * 'user-icon', 'photo'). The matcher accepts any of these so users with no
     * profile picture still satisfy the check.
     */
    private static final String AVATAR_INSIDE_CARD_XPATH =
            ".//img | .//svg " +
            "| .//*[contains(translate(@class, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'avatar')] " +
            "| .//*[contains(@class,'initial')] " +
            "| .//*[contains(@class,'profile-pic') or contains(@class,'profile-image')] " +
            "| .//*[contains(@class,'user-pic')   or contains(@class,'user-image') or contains(@class,'user-icon')] " +
            "| .//*[contains(@class,'photo')]";

    /**
     * True if the user card renders an avatar image or avatar element.
     * Two-stage check:
     *   1) Explicit avatar-like elements (img/svg or classes matching avatar/
     *      initial/profile-pic/photo/etc).
     *   2) Structural heuristic — any element rendered before the name h3 is
     *      the avatar slot. Catches letter-circle avatars whose div uses an
     *      app-specific class we don't know about (e.g. .uavatar, .badge).
     */
    public boolean hasAvatarFor(String name) {
        try {
            WebElement card = userCardByName(name);
            if (!card.findElements(By.xpath(AVATAR_INSIDE_CARD_XPATH)).isEmpty()) {
                return true;
            }
            return !card.findElements(By.xpath(".//h3/preceding::*[ancestor::div[contains(@class,'ucard')]]"))
                    .isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** Same logic as {@link #hasAvatarFor} but for My Matches .mcard. */
    public boolean hasAvatarForMyMatch(String name) {
        try {
            WebElement card = myMatchCardByName(name);
            if (!card.findElements(By.xpath(AVATAR_INSIDE_CARD_XPATH)).isEmpty()) {
                return true;
            }
            return !card.findElements(By.xpath(".//h3/preceding::*[ancestor::div[contains(@class,'mcard')]]"))
                    .isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Diagnostic — returns the raw outerHTML of the .ucard for the given user.
     * Use this when an avatar / field check fails so the test log shows
     * exactly what the card looked like in the DOM.
     */
    public String getUserCardHtmlFor(String name) {
        try {
            return userCardByName(name).getAttribute("outerHTML");
        } catch (Exception e) {
            return "<failed to read card HTML: " + e.getMessage() + ">";
        }
    }

    /** True if the My Matches card exposes a "Message" button or link. */
    public boolean hasMessageButtonFor(String name) {
        try {
            return !myMatchCardByName(name).findElements(By.xpath(
                    ".//a[normalize-space()='Message'] | .//button[normalize-space()='Message']"))
                    .isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Empty-state helpers ──────────────────────────────────────────────────
    // The Matches page renders an empty-state container when a tab has no
    // results. The exact class varies across builds (.hint / .empty /
    // .no-matches / .no-data / .no-results / .not-found / .empty-state), so
    // the locator is intentionally permissive. We also fall back to scanning
    // the whole page body for a "no user / no results / not found" phrase
    // because some renderings put the text directly in the grid container.
    private static final By EMPTY_STATE_LOCATOR = By.xpath(
            "//div[contains(@class,'hint') or contains(@class,'empty') " +
            "    or contains(@class,'no-matches') or contains(@class,'no-data') " +
            "    or contains(@class,'no-results') or contains(@class,'not-found') " +
            "    or contains(@class,'empty-state')]");

    private static boolean looksLikeNoResults(String s) {
        if (s == null) return false;
        String t = s.toLowerCase();
        return t.contains("no user")
            || t.contains("no users")
            || t.contains("not found")
            || t.contains("no match")
            || t.contains("no matches")
            || t.contains("no result")
            || t.contains("no results")
            || t.contains("couldn't find")
            || t.contains("couldnt find")
            || t.contains("didn't find")
            || t.contains("didnt find")
            || t.contains("nothing found")
            || t.contains("0 user")
            || t.contains("0 result");
    }

    /** True if any empty-state container is currently visible. */
    public boolean isEmptyStateVisible() {
        try {
            return driver.findElements(EMPTY_STATE_LOCATOR).stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    /** Text of the visible empty-state message, or "" if none is shown. */
    public String getEmptyStateMessage() {
        try {
            return driver.findElements(EMPTY_STATE_LOCATOR).stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .map(el -> el.getText().trim())
                    .orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * True if the page is showing a "No user is found" / "No results" /
     * "No matches" / "Not found" / "Couldn't find" / "Nothing found" message.
     *
     * Checks (in order):
     *   1. Every displayed empty-state container — there may be more than one
     *      (e.g. an old .hint AND a fresh .no-results). Any one matching wins.
     *   2. The visible body text — catches messages in containers we don't know.
     *   3. The raw HTML page source — catches messages in elements that exist
     *      but are CSS-hidden mid-render, which body.getText() skips.
     */
    public boolean isNoUsersFoundMessageDisplayed() {
        try {
            for (WebElement el : driver.findElements(EMPTY_STATE_LOCATOR)) {
                try {
                    if (el.isDisplayed() && looksLikeNoResults(el.getText())) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // Stale / not interactable — try the next one.
                }
            }
        } catch (Exception ignored) {
            // DOM in flux — fall through to body/source checks.
        }
        try {
            String body = driver.findElement(By.tagName("body")).getText();
            if (looksLikeNoResults(body)) {
                return true;
            }
        } catch (Exception ignored) {
            // Body not ready — fall through to page source.
        }
        try {
            return looksLikeNoResults(driver.getPageSource());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * After clicking Search, wait up to 30 seconds for the result grid to
     * settle — either at least one .ucard whose name contains the search
     * term (case-insensitive), or a "No user is found" empty state.
     * Returns true if a matching card appears, false if 30s passed with
     * no match (regardless of whether the empty state is shown).
     */
    public boolean waitForSearchResults(String term) {
        String lower = term.toLowerCase();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> nameMatches(d, lower) || isNoUsersFoundMessageDisplayed());
        } catch (Exception ignored) {
            // Fall through: caller asserts on the result.
        }
        return nameMatches(driver, lower);
    }

    /**
     * After clicking Search for a term that should yield no results, wait up
     * to 30 seconds for the page to settle into the empty state: zero .ucard
     * elements AND the "No user is found" message visible. The grid can take
     * a moment to re-render after the search button is clicked, so without
     * this wait the assertion fires while the previous cards are still on
     * screen. Returns true once both conditions hold, false on timeout.
     */
    public boolean waitForNoUsersFound() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> d.findElements(
                            By.xpath("//div[contains(@class,'ucard')]")).isEmpty()
                            && isNoUsersFoundMessageDisplayed());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait up to {@code timeoutSeconds} for ANY "no user / no result / not
     * found / no match" style message to appear on the page (either inside
     * an empty-state container or anywhere in the body text). Doesn't care
     * whether stale cards are still on screen — the presence of the message
     * alone is sufficient. Returns true on first match, false on timeout.
     */
    public boolean waitForNoResultsMessage(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(d -> isNoUsersFoundMessageDisplayed());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean nameMatches(WebDriver d, String lowerTerm) {
        try {
            for (WebElement el : d.findElements(
                    By.xpath("//div[contains(@class,'ucard')]/h3"))) {
                if (el.getText().toLowerCase().contains(lowerTerm)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Cards may be repainting — treat as "not yet matched".
        }
        return false;
    }

    public void clickFirstConnectButton() {
        click(firstConnectBtn, "First Connect button");
    }

    // ── My Matches grid (.mcard) ─────────────────────────────────────────────

    public boolean waitForMyMatchesToLoad() {
        By cardLocator = By.xpath("//div[contains(@class,'mcard')]");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(35))
                    .until(ExpectedConditions.visibilityOfElementLocated(cardLocator));
            return !driver.findElements(cardLocator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int getMyMatchCount() {
        return myMatchCards.size();
    }

    /** Names of all rendered .mcard h3 entries in My Matches, in display order. */
    public List<String> getMyMatchNames() {
        return driver.findElements(By.xpath(
                "//div[contains(@class,'mcard')]/h3"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }

    private WebElement myMatchCardByName(String name) {
        return driver.findElement(By.xpath(
                "//div[contains(@class,'mcard')][.//h3[normalize-space()='" + name + "']]"));
    }

    /** True if a .mcard with the given name is currently rendered in My Matches. */
    public boolean hasMyMatchCardFor(String name) {
        return !driver.findElements(By.xpath(
                "//div[contains(@class,'mcard')][.//h3[normalize-space()='" + name + "']]"))
                .isEmpty();
    }

    /**
     * Poll up to {@code timeoutSeconds} for the My Matches card with the given
     * name to appear. After clicking Connect the server can take a beat to
     * persist the connection, so callers should wait rather than asserting
     * immediately. Returns true once the card is rendered, false on timeout.
     */
    public boolean waitForMyMatchCardFor(String name, int timeoutSeconds) {
        By cardLocator = By.xpath(
                "//div[contains(@class,'mcard')][.//h3[normalize-space()='" + name + "']]");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(cardLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Poll up to {@code timeoutSeconds} for the My Matches card count to grow
     * above {@code baselineCount}. Hard-refreshes the page every ~10s and
     * re-clicks My Matches afterwards to force Angular to re-fetch the list.
     * Returns true the moment the count exceeds the baseline, false on timeout.
     */
    public boolean waitForMyMatchesCountAbove(int baselineCount, int timeoutSeconds) {
        By mcards = By.xpath("//div[contains(@class,'mcard')]");
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        long lastRefresh = 0L; // 0 → force a refresh on the first iteration
        while (System.currentTimeMillis() < deadline) {
            try {
                if (driver.findElements(mcards).size() > baselineCount) {
                    return true;
                }
            } catch (Exception ignored) {
                // Stale DOM during repaint — retry on next iteration.
            }
            if (System.currentTimeMillis() - lastRefresh > 10_000L) {
                try {
                    driver.navigate().refresh();
                    new WebDriverWait(driver, Duration.ofSeconds(15))
                            .until(ExpectedConditions.elementToBeClickable(myMatchesTab));
                    myMatchesTab.click();
                    // Cards may legitimately be 0 — don't fail here, just give the grid a moment.
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.visibilityOfElementLocated(mcards));
                    } catch (Exception ignored) {
                        // No cards yet — OK, the outer loop will keep polling.
                    }
                    log.debug("Hard-refreshed page to force My Matches re-fetch (baseline={})", baselineCount);
                } catch (Exception ignored) {
                    // Refresh/tab not ready this instant — keep polling.
                }
                lastRefresh = System.currentTimeMillis();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Poll up to {@code timeoutSeconds} for either:
     *   (a) the My Matches card count to grow above {@code baselineCount}, or
     *   (b) a card with the given target name to be rendered.
     * Returns true the moment either condition holds, false on timeout.
     *
     * Used by TC_060b to prove that clicking Connect caused a new entry in
     * My Matches — name matching alone can be flaky (the server may render
     * the new card slightly differently), so count-based detection is the
     * primary signal.
     *
     * The My Matches list is fetched once when the tab is activated and is
     * cached client-side, so just sitting on the page won't pick up a freshly
     * created connection. This poll re-clicks the My Matches tab every ~10s
     * to force a re-fetch.
     */
    public boolean waitForMyMatchesCountIncreaseOrCard(int baselineCount, String targetName, int timeoutSeconds) {
        By mcards = By.xpath("//div[contains(@class,'mcard')]");
        By targetCard = By.xpath(
                "//div[contains(@class,'mcard')][.//h3[normalize-space()='" + targetName + "']]");
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        long lastRefresh = System.currentTimeMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                if (driver.findElements(mcards).size() > baselineCount
                        || !driver.findElements(targetCard).isEmpty()) {
                    return true;
                }
            } catch (Exception ignored) {
                // Stale DOM during repaint — retry on next iteration.
            }
            // Every ~10s, re-click My Matches to force the list to re-fetch.
            if (System.currentTimeMillis() - lastRefresh > 10_000L) {
                try {
                    myMatchesTab.click();
                    log.debug("Re-clicked My Matches tab to refresh list while waiting for new connection");
                } catch (Exception ignored) {
                    // Tab may not be clickable at this exact instant — keep polling.
                }
                lastRefresh = System.currentTimeMillis();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public String getMyMatchScoreFor(String name) {
        return safeText(myMatchCardByName(name)
                .findElement(By.xpath(".//div[contains(@class,'mscore')]")));
    }

    public String getConnectedDateFor(String name) {
        return safeText(myMatchCardByName(name)
                .findElement(By.xpath(".//div[contains(@class,'mdate')]")));
    }

    /** Click the "Message" link on the .mcard for the given user. */
    public void clickMessageFor(String name) {
        try {
            WebElement link = myMatchCardByName(name).findElement(By.xpath(
                    ".//a[normalize-space()='Message']"));
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();
            log.debug("Clicked Message for: {}", name);
        } catch (Exception e) {
            log.warn("clickMessageFor failed for '{}': {}", name, e.getMessage());
        }
    }

    public void clickFirstMessageLink() {
        click(firstMessageLink, "First Message link");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean isDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private String safeText(WebElement el) {
        try {
            return el.getText().trim();
        } catch (Exception e) {
            return "";
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