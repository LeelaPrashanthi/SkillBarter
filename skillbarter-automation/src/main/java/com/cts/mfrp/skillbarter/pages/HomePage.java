package com.cts.mfrp.skillbarter.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the SkillBarter Landing Page (REQ-2.1).
 * Covers TC_001 – TC_007 (TS_001).
 *
 * The app is built with Angular 17. Locators target the actual rendered DOM
 * (component class names like .nav, .hero, .feature-card, .sb-toggle) rather
 * than spec-text only, so they remain stable across re-renders.
 */
public class HomePage {

    private static final Logger log = LogManager.getLogger(HomePage.class);
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Top Navigation (REQ-2.1.1) ────────────────────────────────────────────
    @FindBy(xpath = "//nav[contains(@class,'nav')]/div[contains(@class,'nav-brand')]")
    private WebElement navBrand;

    @FindBy(xpath = "//nav[contains(@class,'nav')]//div[contains(@class,'nav-links')]/a[@href='#about']")
    private WebElement navAbout;

    @FindBy(xpath = "//nav[contains(@class,'nav')]//div[contains(@class,'nav-links')]/a[@href='#features']")
    private WebElement navFeatures;

    @FindBy(xpath = "//nav[contains(@class,'nav')]//button[contains(@class,'nav-theme-btn')]")
    private WebElement themeToggleBtn;

    // Note: per REQ-2.1.1 this button should navigate to /signup, but the
    // current build routes it to /login. We assert on its presence + the
    // resulting auth page; see HomePageTest for the URL assertion.
    @FindBy(xpath = "//nav[contains(@class,'nav')]//div[contains(@class,'nav-actions')]/a[contains(@class,'btn-primary')]")
    private WebElement navGetStartedBtn;

    // ── Hero Section (REQ-2.1.2) ──────────────────────────────────────────────
    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'hero-content')]//div[contains(@class,'hero-tag')]")
    private WebElement heroBadge;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'hero-content')]/h1")
    private WebElement heroHeadline;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'hero-content')]/p")
    private WebElement heroSubtext;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'hero-btns')]/a[contains(@class,'btn-primary')]")
    private WebElement heroGetStartedFreeBtn;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'hero-btns')]/a[contains(@class,'btn-ghost')]")
    private WebElement heroSignInBtn;

    // ── Preview Cards (REQ-2.1.3) ─────────────────────────────────────────────
    // Card 1: "TODAY — 12 new matches across 4 categories."
    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'visual-card-top')]")
    private WebElement topMatchesCard;

    // Card 2: Brand card – SkillBarter logo + "Match. Learn. Grow."
    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'visual-card-main')]")
    private WebElement brandCard;

    // Card 3: "Sessions booked" + progress bar
    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'visual-card-bottom')]")
    private WebElement momentumCard;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'visual-card-bottom')]//div[contains(@class,'mini-bar')]")
    private WebElement momentumProgressBar;

    @FindBy(xpath = "//section[contains(@class,'hero')]//div[contains(@class,'visual-card-main')]//div[contains(@class,'ring-sub')]")
    private WebElement brandTagline;

    // ── About Section (REQ-2.1.5) ─────────────────────────────────────────────
    @FindBy(xpath = "//section[@id='about']/h2")
    private WebElement aboutHeading;

    @FindBy(xpath = "//section[@id='about']/p")
    private WebElement aboutSubtext;

    // ── Features Section (REQ-2.1.6) ──────────────────────────────────────────
    @FindBy(xpath = "//section[@id='features']/h2")
    private WebElement featuresHeading;

    @FindBy(xpath = "//section[@id='features']//div[contains(@class,'features-grid')]/div[contains(@class,'feature-card')]")
    private List<WebElement> featureCards;

    @FindBy(xpath = "//section[@id='features']//div[contains(@class,'features-grid')]/div[contains(@class,'feature-card')]/h3")
    private List<WebElement> featureCardTitles;

    // ── CTA Section (REQ-2.1.7) ───────────────────────────────────────────────
    @FindBy(xpath = "//section[contains(concat(' ',normalize-space(@class),' '),' cta ')]/h2")
    private WebElement ctaHeading;

    @FindBy(xpath = "//section[contains(concat(' ',normalize-space(@class),' '),' cta ')]//div[contains(@class,'cta-btns')]/a[contains(@class,'btn-primary')]")
    private WebElement ctaCreateAccountBtn;

    @FindBy(xpath = "//section[contains(concat(' ',normalize-space(@class),' '),' cta ')]//div[contains(@class,'cta-btns')]/a[contains(@class,'btn-ghost')]")
    private WebElement ctaSignInBtn;

    // ── Chatbot Button (REQ-2.1.4) ────────────────────────────────────────────
    @FindBy(xpath = "//app-chatbot//div[contains(@class,'sb-chatbot')]/button[contains(@class,'sb-toggle')]")
    private WebElement chatbotBtn;

    // ── Footer (REQ-2.1.8) ────────────────────────────────────────────────────
    @FindBy(xpath = "//footer[contains(@class,'footer')]")
    private WebElement footer;

    @FindBy(xpath = "//footer[contains(@class,'footer')]/div[contains(@class,'footer-brand')]")
    private WebElement footerBrand;

    @FindBy(xpath = "//footer[contains(@class,'footer')]/p")
    private WebElement footerCopyright;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        PageFactory.initElements(driver, this);
    }

    // ── Page Load ─────────────────────────────────────────────────────────────

    /** Wait until the landing page is interactive – the H1 headline is visible. */
    public boolean waitForLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(heroHeadline));
            return true;
        } catch (TimeoutException e) {
            log.warn("Landing page did not finish loading within {}s", WAIT_TIMEOUT.getSeconds());
            return false;
        }
    }

    // ── Top Navigation ────────────────────────────────────────────────────────

    public boolean isNavBrandVisible()      { return waitVisible(navBrand); }
    public boolean isNavAboutVisible()      { return waitVisible(navAbout); }
    public boolean isNavFeaturesVisible()   { return waitVisible(navFeatures); }
    public boolean isThemeToggleVisible()   { return waitVisible(themeToggleBtn); }
    public boolean isNavGetStartedVisible() { return waitVisible(navGetStartedBtn); }

    public String getNavBrandText()         { return getText(navBrand); }
    public String getNavGetStartedText()    { return getText(navGetStartedBtn); }
    public String getNavGetStartedHref() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(navGetStartedBtn))
                    .getAttribute("href");
        } catch (Exception e) { return ""; }
    }

    public void clickNavBrand()             { click(navBrand, "nav brand (SkillBarter)"); }
    public void clickNavAbout()             { click(navAbout, "nav About"); }
    public void clickNavFeatures()          { click(navFeatures, "nav Features"); }
    public void clickNavGetStarted()        { click(navGetStartedBtn, "nav Get started"); }

    /** Returns the aria-label of the theme toggle (e.g. "Switch to light mode"). */
    public String getThemeToggleAriaLabel() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(themeToggleBtn))
                    .getAttribute("aria-label");
        } catch (Exception e) { return ""; }
    }

    public void clickThemeToggle()          { click(themeToggleBtn, "theme toggle"); }

    // ── Hero ──────────────────────────────────────────────────────────────────

    public boolean isHeroBadgeVisible()     { return waitVisible(heroBadge); }
    public boolean isHeroHeadlineVisible()  { return waitVisible(heroHeadline); }
    public boolean isHeroSubtextVisible()   { return waitVisible(heroSubtext); }

    public String getHeroBadgeText()        { return getText(heroBadge); }
    /** Headline contains <br>; getText already collapses to a single line with newline. */
    public String getHeroHeadlineText()     { return getText(heroHeadline).replace("\n", " "); }
    public String getHeroSubtextText()      { return getText(heroSubtext); }

    public boolean isHeroGetStartedFreeVisible() { return waitVisible(heroGetStartedFreeBtn); }
    public boolean isHeroSignInVisible()    { return waitVisible(heroSignInBtn); }

    public String getHeroGetStartedFreeHref() {
        try { return wait.until(ExpectedConditions.visibilityOf(heroGetStartedFreeBtn)).getAttribute("href"); }
        catch (Exception e) { return ""; }
    }
    public String getHeroSignInHref() {
        try { return wait.until(ExpectedConditions.visibilityOf(heroSignInBtn)).getAttribute("href"); }
        catch (Exception e) { return ""; }
    }

    public void clickHeroGetStartedFree()   { click(heroGetStartedFreeBtn, "Hero Get Started Free"); }
    public void clickHeroSignIn()           { click(heroSignInBtn, "Hero Sign In"); }

    // ── Preview Cards ─────────────────────────────────────────────────────────

    public boolean isTopMatchesCardVisible() { return waitVisible(topMatchesCard); }
    public boolean isBrandCardVisible()      { return waitVisible(brandCard); }
    public boolean isMomentumCardVisible()   { return waitVisible(momentumCard); }
    public boolean isMomentumProgressBarVisible() { return waitVisible(momentumProgressBar); }

    public String getTopMatchesCardText()    { return getText(topMatchesCard); }
    public String getBrandTaglineText()      { return getText(brandTagline); }
    public String getMomentumCardText()      { return getText(momentumCard); }

    // ── About ─────────────────────────────────────────────────────────────────

    public boolean isAboutHeadingVisible() {
        scrollTo(aboutHeading);
        return waitVisible(aboutHeading);
    }

    public boolean isAboutSubtextVisible() {
        scrollTo(aboutSubtext);
        return waitVisible(aboutSubtext);
    }

    public String getAboutHeadingText() { return getText(aboutHeading); }
    public String getAboutSubtextText() { return getText(aboutSubtext); }

    // ── Features ──────────────────────────────────────────────────────────────

    public boolean isFeaturesHeadingVisible() {
        scrollTo(featuresHeading);
        return waitVisible(featuresHeading);
    }

    public String getFeaturesHeadingText() { return getText(featuresHeading); }

    public int getFeatureCardCount() {
        scrollTo(featuresHeading);
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(featureCards));
        } catch (Exception ignored) {}
        return featureCards.size();
    }

    /** Returns the 4 feature card titles in DOM order. */
    public List<String> getFeatureCardTitles() {
        scrollTo(featuresHeading);
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(featureCardTitles));
        } catch (Exception ignored) {}
        return featureCardTitles.stream().map(this::getText).toList();
    }

    // ── CTA ───────────────────────────────────────────────────────────────────

    public boolean isCtaHeadingVisible() {
        scrollTo(ctaHeading);
        return waitVisible(ctaHeading);
    }

    public String getCtaHeadingText() { return getText(ctaHeading); }

    public boolean isCtaCreateAccountVisible() { return waitVisible(ctaCreateAccountBtn); }
    public boolean isCtaSignInVisible()        { return waitVisible(ctaSignInBtn); }

    public String getCtaCreateAccountHref() {
        try { return wait.until(ExpectedConditions.visibilityOf(ctaCreateAccountBtn)).getAttribute("href"); }
        catch (Exception e) { return ""; }
    }
    public String getCtaSignInHref() {
        try { return wait.until(ExpectedConditions.visibilityOf(ctaSignInBtn)).getAttribute("href"); }
        catch (Exception e) { return ""; }
    }

    public void clickCtaCreateAccount() { click(ctaCreateAccountBtn, "CTA Create Free Account"); }
    public void clickCtaSignIn()        { click(ctaSignInBtn, "CTA Sign In"); }

    // ── Chatbot ───────────────────────────────────────────────────────────────

    public boolean isChatbotButtonVisible() { return waitVisible(chatbotBtn); }

    public void clickChatbot() { click(chatbotBtn, "chatbot toggle"); }

    // ── Footer ────────────────────────────────────────────────────────────────

    public boolean isFooterVisible() {
        scrollTo(footer);
        return waitVisible(footer);
    }

    public boolean isFooterBrandVisible() {
        scrollTo(footer);
        return waitVisible(footerBrand);
    }

    public String getFooterBrandText()       { return getText(footerBrand); }
    public String getFooterCopyrightText() {
        scrollTo(footer);
        return getText(footerCopyright);
    }

    // ── Dark Mode ─────────────────────────────────────────────────────────────

    /**
     * Probe whether the page is currently in dark mode.
     * The Angular app does not toggle a `.dark` class on &lt;html&gt;; instead
     * it swaps CSS custom properties on :root and updates the toggle button's
     * aria-label. We sample both signals so the check survives either approach.
     */
    public boolean isDarkModeActive() {
        try {
            // 1) Background colour heuristic – dark mode background is near-black.
            String bg = String.valueOf(((JavascriptExecutor) driver).executeScript(
                    "return getComputedStyle(document.body).backgroundColor || " +
                    "getComputedStyle(document.documentElement).backgroundColor;"));
            if (isDarkColor(bg)) return true;

            // 2) Class / data-theme fallback (in case the build adds one later).
            String htmlClass = String.valueOf(((JavascriptExecutor) driver)
                    .executeScript("return document.documentElement.className || '';"));
            String bodyClass = String.valueOf(((JavascriptExecutor) driver)
                    .executeScript("return document.body.className || '';"));
            String dataTheme = String.valueOf(((JavascriptExecutor) driver)
                    .executeScript("return document.documentElement.getAttribute('data-theme') || '';"));
            String combined = (htmlClass + " " + bodyClass + " " + dataTheme).toLowerCase();
            return combined.contains("dark");
        } catch (Exception e) {
            return false;
        }
    }

    /** Parses "rgb(r, g, b)" / "rgba(r, g, b, a)" – returns true when luminance is low. */
    private boolean isDarkColor(String rgb) {
        if (rgb == null || rgb.isEmpty()) return false;
        try {
            int open  = rgb.indexOf('(');
            int close = rgb.indexOf(')');
            if (open < 0 || close < 0) return false;
            String[] parts = rgb.substring(open + 1, close).split(",");
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            // Perceived luminance (Rec. 601). Below 80/255 = clearly dark.
            double lum = 0.299 * r + 0.587 * g + 0.114 * b;
            return lum < 80.0;
        } catch (Exception e) {
            return false;
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private boolean waitVisible(WebElement el) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(el)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private String getText(WebElement el) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(el)).getText().trim();
        } catch (Exception e) {
            log.warn("getText failed: {}", e.getMessage());
            return "";
        }
    }

    private void click(WebElement el, String name) {
        try {
            WebElement clickable = wait.until(ExpectedConditions.elementToBeClickable(el));
            try {
                clickable.click();
            } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickable);
            }
            log.debug("Clicked: {}", name);
        } catch (Exception e) {
            log.warn("Click failed on '{}': {}", name, e.getMessage());
        }
    }

    private void scrollTo(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', behavior:'instant'});", el);
        } catch (Exception ignored) {}
    }
}
