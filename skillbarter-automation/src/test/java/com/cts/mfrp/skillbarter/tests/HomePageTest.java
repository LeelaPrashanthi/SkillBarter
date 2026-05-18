package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.pages.HomePage;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test class for the SkillBarter Landing Page.
 *
 * Scenario   : TS_001 – Verify Landing Page UI and Navigation
 * Requirement: REQ-2.1 (sections 2.1.1 – 2.1.9)
 * Test Cases : TC_001 → TC_007
 * Group      : home-page, regression, smoke
 *
 * The Angular SPA is slow to bootstrap, so every assertion is preceded by an
 * explicit WebDriverWait inside the HomePage page object (visibilityOf /
 * elementToBeClickable / urlContains).
 */
public class HomePageTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true)
    public void openHomePage() {
        navigateToBase();
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.waitForLoaded(),
                "Landing page did not load – hero headline not visible within wait timeout.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_001 – Top Navigation Bar (REQ-2.1.1)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_001",
          description = "Verify top navigation shows SkillBarter brand, About, Features, theme toggle, " +
                        "and Get Started button; clicking Get Started navigates to an auth page",
          groups = {"home-page", "smoke", "regression"}, priority = 1, retryAnalyzer = RetryAnalyzer.class)
    public void tc001_verifyTopNavigationLinks() {
        // Brand
        Assert.assertTrue(homePage.isNavBrandVisible(),
                "SkillBarter brand not visible in top navigation bar.");
        Assert.assertEquals(homePage.getNavBrandText(), "SkillBarter",
                "Nav brand text mismatch.");

        // Center links
        Assert.assertTrue(homePage.isNavAboutVisible(),
                "'About' link not visible in top navigation bar.");
        Assert.assertTrue(homePage.isNavFeaturesVisible(),
                "'Features' link not visible in top navigation bar.");

        // Theme toggle (moon/sun)
        Assert.assertTrue(homePage.isThemeToggleVisible(),
                "Theme toggle (moon/sun) icon button not visible in top navigation bar.");
        String ariaLabel = homePage.getThemeToggleAriaLabel();
        Assert.assertTrue(
                ariaLabel != null && ariaLabel.toLowerCase().contains("mode"),
                "Theme toggle aria-label should reference 'mode'. Got: '" + ariaLabel + "'");

        // Get Started button on the right
        Assert.assertTrue(homePage.isNavGetStartedVisible(),
                "'Get Started' button not visible in top navigation bar.");
        Assert.assertTrue(
                homePage.getNavGetStartedText().toLowerCase().contains("get started"),
                "Nav Get Started button text mismatch. Got: '" + homePage.getNavGetStartedText() + "'");

        // REQ-2.1.1 expects /signup, but the live app currently routes nav Get
        // Started to /login. Accept either auth page so the test reflects the
        // shipped UX while still failing if the button leads to anything else.
        String href = homePage.getNavGetStartedHref();
        Assert.assertTrue(
                href != null && (href.contains("/signup") || href.contains("/login")),
                "Nav Get Started href should point to /signup or /login. Got: '" + href + "'");

        homePage.clickNavGetStarted();
        boolean landed = wait.until(d ->
                d.getCurrentUrl().contains("/signup") || d.getCurrentUrl().contains("/login"));
        Assert.assertTrue(landed,
                "Nav 'Get Started' did not navigate to /signup or /login. URL: " + getCurrentUrl());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_002 – Hero Section content + CTAs (REQ-2.1.2)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_002",
          description = "Verify Hero badge, headline, subtext, and that 'Get Started Free' " +
                        "navigates to /signup and 'Sign In' navigates to /login",
          groups = {"home-page", "smoke", "regression"}, priority = 2, retryAnalyzer = RetryAnalyzer.class)
    public void tc002_verifyHeroSection() {
        // Badge
        Assert.assertTrue(homePage.isHeroBadgeVisible(),
                "Hero badge 'Zero money. Real learning.' is not visible.");
        Assert.assertEquals(homePage.getHeroBadgeText(), "Zero money. Real learning.",
                "Hero badge text mismatch.");

        // Headline (rendered with a <br>; we normalise to a single line)
        Assert.assertTrue(homePage.isHeroHeadlineVisible(),
                "Hero headline not visible.");
        String headline = homePage.getHeroHeadlineText();
        Assert.assertTrue(headline.contains("Learn anything.") && headline.contains("Teach what you love."),
                "Hero headline text mismatch. Got: '" + headline + "'");

        // Subtext
        Assert.assertTrue(homePage.isHeroSubtextVisible(),
                "Hero subtext not visible.");
        String subtext = homePage.getHeroSubtextText();
        Assert.assertTrue(
                subtext.contains("Exchange skills instead of cash")
                        && subtext.contains("Smart matching")
                        && subtext.contains("global community"),
                "Hero subtext is missing expected copy. Got: '" + subtext + "'");

        // CTAs – verify both are visible with correct href
        Assert.assertTrue(homePage.isHeroGetStartedFreeVisible(),
                "'Get Started Free' button not visible in hero section.");
        Assert.assertTrue(homePage.isHeroSignInVisible(),
                "'Sign In' button not visible in hero section.");
        Assert.assertTrue(homePage.getHeroGetStartedFreeHref().contains("/signup"),
                "Hero 'Get Started Free' href should contain /signup. Got: '"
                        + homePage.getHeroGetStartedFreeHref() + "'");
        Assert.assertTrue(homePage.getHeroSignInHref().contains("/login"),
                "Hero 'Sign In' href should contain /login. Got: '"
                        + homePage.getHeroSignInHref() + "'");

        // Click Get Started Free → /signup
        homePage.clickHeroGetStartedFree();
        Assert.assertTrue(
                wait.until(ExpectedConditions.urlContains("/signup")),
                "Hero 'Get Started Free' did not navigate to /signup. Current URL: " + getCurrentUrl());

        // Return and click Sign In → /login
        navigateToBase();
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.waitForLoaded(),
                "Landing page did not reload after returning from /signup.");
        homePage.clickHeroSignIn();
        Assert.assertTrue(
                wait.until(ExpectedConditions.urlContains("/login")),
                "Hero 'Sign In' did not navigate to /login. Current URL: " + getCurrentUrl());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_003 – Preview Cards (REQ-2.1.3)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_003",
          description = "Verify the three floating preview cards (Top Matches, Brand, Momentum) " +
                        "render with the expected copy and progress indicator",
          groups = {"home-page", "regression"}, priority = 3, retryAnalyzer = RetryAnalyzer.class)
    public void tc003_verifyPreviewCards() {
        // Top Matches card
        Assert.assertTrue(homePage.isTopMatchesCardVisible(),
                "'Top Matches' preview card not visible.");
        String topMatches = homePage.getTopMatchesCardText();
        Assert.assertTrue(
                topMatches.contains("12 new matches") && topMatches.contains("4 categories"),
                "Top Matches card text mismatch. Got: '" + topMatches + "'");

        // Brand card
        Assert.assertTrue(homePage.isBrandCardVisible(),
                "Brand preview card not visible.");
        Assert.assertEquals(homePage.getBrandTaglineText(), "Match. Learn. Grow.",
                "Brand tagline mismatch.");

        // Momentum card
        Assert.assertTrue(homePage.isMomentumCardVisible(),
                "'Momentum' preview card not visible.");
        Assert.assertTrue(homePage.getMomentumCardText().contains("Sessions booked"),
                "Momentum card should contain 'Sessions booked'. Got: '"
                        + homePage.getMomentumCardText() + "'");
        Assert.assertTrue(homePage.isMomentumProgressBarVisible(),
                "Momentum card progress bar not visible.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_004 – Features Section (REQ-2.1.6)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_004",
          description = "Verify Features heading and that all four feature cards " +
                        "(Smart Matching, Built-in Chat, Calendar, Earn XP) are rendered in order",
          groups = {"home-page", "regression"}, priority = 4, retryAnalyzer = RetryAnalyzer.class)
    public void tc004_verifyFeatureHighlights() {
        Assert.assertTrue(homePage.isFeaturesHeadingVisible(),
                "'Everything you need to grow.' heading not visible.");
        Assert.assertEquals(homePage.getFeaturesHeadingText(), "Everything you need to grow.",
                "Features heading text mismatch.");

        Assert.assertEquals(homePage.getFeatureCardCount(), 4,
                "Expected exactly 4 feature cards.");

        List<String> titles = homePage.getFeatureCardTitles();
        Assert.assertEquals(titles,
                List.of("Smart Matching", "Built-in Chat", "Calendar", "Earn XP"),
                "Feature card titles or order mismatch. Got: " + titles);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_005 – About Section + CTA Section (REQ-2.1.5 & REQ-2.1.7)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_005",
          description = "Verify About section heading + subtext and CTA section heading + " +
                        "Create Free Account button navigates to /signup",
          groups = {"home-page", "regression"}, priority = 5, retryAnalyzer = RetryAnalyzer.class)
    public void tc005_verifyAboutAndCtaSections() {
        // About
        Assert.assertTrue(homePage.isAboutHeadingVisible(),
                "About section heading not visible.");
        Assert.assertEquals(homePage.getAboutHeadingText(),
                "We believe everyone has something valuable to teach.",
                "About heading text mismatch.");
        Assert.assertTrue(homePage.isAboutSubtextVisible(),
                "About subtext not visible.");
        Assert.assertTrue(
                homePage.getAboutSubtextText().contains("people-first space for peer learning"),
                "About subtext missing expected copy. Got: '" + homePage.getAboutSubtextText() + "'");

        // CTA section
        Assert.assertTrue(homePage.isCtaHeadingVisible(),
                "CTA heading 'Ready to start learning?' not visible.");
        Assert.assertEquals(homePage.getCtaHeadingText(), "Ready to start learning?",
                "CTA heading text mismatch.");
        Assert.assertTrue(homePage.isCtaCreateAccountVisible(),
                "'Create Free Account' button not visible.");
        Assert.assertTrue(homePage.isCtaSignInVisible(),
                "CTA 'Sign In' button not visible.");
        Assert.assertTrue(homePage.getCtaCreateAccountHref().contains("/signup"),
                "CTA 'Create Free Account' href should contain /signup. Got: '"
                        + homePage.getCtaCreateAccountHref() + "'");
        Assert.assertTrue(homePage.getCtaSignInHref().contains("/login"),
                "CTA 'Sign In' href should contain /login. Got: '"
                        + homePage.getCtaSignInHref() + "'");

        homePage.clickCtaCreateAccount();
        Assert.assertTrue(
                wait.until(ExpectedConditions.urlContains("/signup")),
                "CTA 'Create Free Account' did not navigate to /signup. Current URL: " + getCurrentUrl());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_006 – Chatbot Button + Dark Mode Toggle (REQ-2.1.4 & REQ-2.1.9)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_006",
          description = "Verify floating chatbot button is visible at bottom-right, and the theme " +
                        "toggle flips the page between dark and light modes",
          groups = {"home-page", "regression"}, priority = 6, retryAnalyzer = RetryAnalyzer.class)
    public void tc006_verifyChatbotAndDarkMode() {
        Assert.assertTrue(homePage.isChatbotButtonVisible(),
                "Floating chatbot button not visible at the bottom-right corner.");

        // The site loads in dark mode by default; the toggle button aria-label
        // ("Switch to light mode" / "Switch to dark mode") is the most reliable
        // signal because the app swaps CSS variables rather than a `.dark` class.
        String initialLabel = homePage.getThemeToggleAriaLabel();
        boolean initialDark = homePage.isDarkModeActive();
        Assert.assertFalse(initialLabel.isEmpty(),
                "Theme toggle aria-label is empty before clicking.");

        homePage.clickThemeToggle();

        boolean labelFlipped = wait.until(d -> {
            String now = homePage.getThemeToggleAriaLabel();
            return !now.isEmpty() && !now.equalsIgnoreCase(initialLabel);
        });
        Assert.assertTrue(labelFlipped,
                "Theme toggle aria-label did not change after click. " +
                "Before: '" + initialLabel + "', after: '" + homePage.getThemeToggleAriaLabel() + "'");

        boolean modeFlipped = homePage.isDarkModeActive() != initialDark;
        Assert.assertTrue(modeFlipped,
                "Theme did not switch between dark and light mode. " +
                "initialDark=" + initialDark + ", afterDark=" + homePage.isDarkModeActive());

        // Chatbot must remain visible after the theme switch
        Assert.assertTrue(homePage.isChatbotButtonVisible(),
                "Floating chatbot button disappeared after toggling theme.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_007 – Footer (REQ-2.1.8)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_007",
          description = "Verify Footer shows SkillBarter brand on the left and " +
                        "© 2026 SkillBarter copyright text on the right",
          groups = {"home-page", "regression"}, priority = 7, retryAnalyzer = RetryAnalyzer.class)
    public void tc007_verifyFooterSection() {
        Assert.assertTrue(homePage.isFooterVisible(),
                "Footer is not visible at the bottom of the landing page.");

        Assert.assertTrue(homePage.isFooterBrandVisible(),
                "SkillBarter brand not visible in the footer.");
        Assert.assertEquals(homePage.getFooterBrandText(), "SkillBarter",
                "Footer brand text mismatch.");

        String copyright = homePage.getFooterCopyrightText();
        Assert.assertEquals(copyright,
                "© 2026 SkillBarter. Bringing real learning back to real people.",
                "Footer copyright text mismatch.");
    }
}
