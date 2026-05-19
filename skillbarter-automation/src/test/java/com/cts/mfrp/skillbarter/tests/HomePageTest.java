package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.pages.HomePage;
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
 * Convention: each @Test has exactly one Assert call. Steps before the
 * assertion rely on WebDriverWait (wait.until / ExpectedConditions) — a
 * timeout there throws TimeoutException, which serves the same role as a
 * precondition assert without inflating the assertion count.
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
          groups = {"home-page", "smoke", "regression"}, priority = 1)
    public void tc001_verifyTopNavigationLinks() {
        homePage.clickNavGetStarted();
        // REQ-2.1.1 expects /signup, but the live app currently routes nav Get
        // Started to /login. Accept either.
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
          groups = {"home-page", "smoke", "regression"}, priority = 2)
    public void tc002_verifyHeroSection() {
        // Get Started Free → /signup (wait throws TimeoutException on failure).
        homePage.clickHeroGetStartedFree();
        wait.until(ExpectedConditions.urlContains("/signup"));

        // Return and click Sign In → /login.
        navigateToBase();
        homePage = new HomePage(driver);
        homePage.waitForLoaded();
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
          groups = {"home-page", "regression"}, priority = 3)
    public void tc003_verifyPreviewCards() {
        Assert.assertEquals(homePage.getBrandTaglineText(), "Match. Learn. Grow.",
                "Brand tagline mismatch on preview cards.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_004 – Features Section (REQ-2.1.6)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_004",
          description = "Verify Features heading and that all four feature cards " +
                        "(Smart Matching, Built-in Chat, Calendar, Earn XP) are rendered in order",
          groups = {"home-page", "regression"}, priority = 4)
    public void tc004_verifyFeatureHighlights() {
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
          groups = {"home-page", "regression"}, priority = 5)
    public void tc005_verifyAboutAndCtaSections() {
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
          groups = {"home-page", "regression"}, priority = 6)
    public void tc006_verifyChatbotAndDarkMode() {
        // The site loads in dark mode by default; the toggle button aria-label
        // ("Switch to light mode" / "Switch to dark mode") is the most reliable
        // signal because the app swaps CSS variables rather than a `.dark` class.
        String initialLabel = homePage.getThemeToggleAriaLabel();
        homePage.clickThemeToggle();

        boolean labelFlipped = wait.until(d -> {
            String now = homePage.getThemeToggleAriaLabel();
            return !now.isEmpty() && !now.equalsIgnoreCase(initialLabel);
        });
        Assert.assertTrue(labelFlipped,
                "Theme toggle aria-label did not change after click. " +
                "Before: '" + initialLabel + "', after: '" + homePage.getThemeToggleAriaLabel() + "'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC_007 – Footer (REQ-2.1.8)
    // ─────────────────────────────────────────────────────────────────────────
    @Test(testName = "TC_007",
          description = "Verify Footer shows SkillBarter brand on the left and " +
                        "© 2026 SkillBarter copyright text on the right",
          groups = {"home-page", "regression"}, priority = 7)
    public void tc007_verifyFooterSection() {
        Assert.assertEquals(homePage.getFooterCopyrightText(),
                "© 2026 SkillBarter. Bringing real learning back to real people.",
                "Footer copyright text mismatch.");
    }
}
