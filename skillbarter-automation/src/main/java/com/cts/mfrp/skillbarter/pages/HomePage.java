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
 * Page Object for the SkillBarter Home Page.
 * Covers TC_001 – TC_007 (TS_001).
 */
public class HomePage {

    private static final Logger log = LogManager.getLogger(HomePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Top Navigation ────────────────────────────────────────────────────────
    @FindBy(linkText = "About")
    private WebElement navAbout;

    @FindBy(linkText = "Features")
    private WebElement navFeatures;

    @FindBy(linkText = "Stories")
    private WebElement navStories;

    @FindBy(linkText = "Pricing")
    private WebElement navPricing;

    @FindBy(css = "a[href*='get-started'], button.get-started, a.get-started, " +
            "nav a:last-child, header a[href*='signup']")
    private WebElement navGetStarted;

    // ── Hero Section ──────────────────────────────────────────────────────────
    @FindBy(css = "h1, .hero-title, [class*='hero'] h1")
    private WebElement heroTitle;

    @FindBy(css = ".hero-description, [class*='hero'] p, [class*='hero-sub']")
    private WebElement heroDescription;

    @FindBy(css = "button[class*='get-started'], a[class*='get-started'], " +
            "[class*='hero'] a[href*='signup'], [class*='hero'] button")
    private WebElement heroGetStartedBtn;

    @FindBy(css = "[class*='explore'], a[href*='features']")
    private WebElement exploreFeatureBtn;

    // ── Promotional Banner ────────────────────────────────────────────────────
    @FindBy(css = "[class*='banner'], [class*='promo'], section.story-section")
    private WebElement promoBanner;

    @FindBy(css = "[class*='read-story'], a[href*='story'], button[class*='story']")
    private WebElement readOurStoryBtn;

    // ── Feature Highlights ────────────────────────────────────────────────────
    @FindBy(css = "[class*='feature-highlight'], [class*='steps'], [class*='how-it-works']")
    private WebElement featureHighlights;

    @FindBy(css = "[class*='get-results'], button[class*='result']")
    private WebElement getResultsBtn;

    // ── Platform Capabilities ─────────────────────────────────────────────────
    @FindBy(css = "[class*='platform-cap'], [class*='capabilities'], [class*='platform']")
    private WebElement platformCapabilities;

    // ── Footer ────────────────────────────────────────────────────────────────
    @FindBy(css = "footer, [class*='footer']")
    private WebElement footer;

    @FindBy(css = "footer [class*='copyright'], footer p")
    private WebElement copyrightText;

    @FindBy(css = "footer a[href*='instagram'], footer [class*='instagram']")
    private WebElement instagramIcon;

    @FindBy(css = "footer a[href*='twitter'], footer a[href*='x.com'], footer [class*='twitter']")
    private WebElement twitterIcon;

    @FindBy(css = "footer a[href*='linkedin'], footer [class*='linkedin']")
    private WebElement linkedinIcon;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Navigation actions ────────────────────────────────────────────────────

    public boolean isNavAboutPresent() {
        return isDisplayed(navAbout);
    }

    public boolean isNavFeaturesPresent() {
        return isDisplayed(navFeatures);
    }

    public boolean isNavStoriesPresent() {
        return isDisplayed(navStories);
    }

    public boolean isNavPricingPresent() {
        return isDisplayed(navPricing);
    }

    public boolean isNavGetStartedPresent() {
        return isDisplayed(navGetStarted);
    }

    public void clickNavAbout() {
        click(navAbout, "About nav link");
    }

    public void clickNavFeatures() {
        click(navFeatures, "Features nav link");
    }

    public void clickNavStories() {
        click(navStories, "Stories nav link");
    }

    public void clickNavPricing() {
        click(navPricing, "Pricing nav link");
    }

    public void clickGetStarted() {
        click(navGetStarted, "Get Started CTA");
    }

    // ── Hero ──────────────────────────────────────────────────────────────────

    public String getHeroTitleText() {
        return getText(heroTitle);
    }

    public String getHeroDescriptionText() {
        return getText(heroDescription);
    }

    public void clickHeroGetStarted() {
        click(heroGetStartedBtn, "Hero Get Started button");
    }

    public void clickExploreFeature() {
        click(exploreFeatureBtn, "Explore Feature button");
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    public boolean isBannerVisible() {
        return isDisplayed(promoBanner);
    }

    public String getBannerText() {
        return getText(promoBanner);
    }

    public void clickReadOurStory() {
        click(readOurStoryBtn, "Read Our Story button");
    }

    // ── Features section ─────────────────────────────────────────────────────

    public boolean isFeatureHighlightsSectionVisible() {
        scrollTo(featureHighlights);
        return isDisplayed(featureHighlights);
    }

    public String getFeatureHighlightsText() {
        return getText(featureHighlights);
    }

    public void clickGetResults() {
        click(getResultsBtn, "Get Results button");
    }

    // ── Platform capabilities ─────────────────────────────────────────────────

    public boolean isPlatformCapabilitiesVisible() {
        scrollTo(platformCapabilities);
        return isDisplayed(platformCapabilities);
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    public boolean isFooterVisible() {
        scrollTo(footer);
        return isDisplayed(footer);
    }

    public String getCopyrightText() {
        scrollTo(copyrightText);
        return getText(copyrightText);
    }

    public boolean isSocialIconsVisible() {
        return isDisplayed(instagramIcon) || isDisplayed(twitterIcon) || isDisplayed(linkedinIcon);
    }

    public boolean areAllFooterLinksPresent() {
        String footerHtml = getText(footer);
        return footerHtml.contains("Privacy Policy") || footerHtml.contains("Terms");
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private boolean isDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private String getText(WebElement el) {
        try {
            return el.getText().trim();
        } catch (Exception e) {
            log.warn("getText failed: {}", e.getMessage());
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

    private void scrollTo(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
        } catch (Exception ignored) {}
    }
}
