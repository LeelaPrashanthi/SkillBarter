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
 * Page Object for the Subscriptions page.
 * Covers TC_051 – TC_055 (TS_011).
 */
public class SubscriptionsPage {

    private static final Logger log = LogManager.getLogger(SubscriptionsPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1, h2, [class*='page-title'], [class*='subscriptions-title']")
    private WebElement pageTitle;

    // Pro Plan
    @FindBy(css = "[class*='pro-plan'], [class*='plan-card']:first-of-type")
    private WebElement proPlanCard;

    @FindBy(css = "[class*='pro-plan'] button[class*='subscribe'], " +
            "[class*='plan-card']:first-of-type button")
    private WebElement proSubscribeBtn;

    // Elite Plan
    @FindBy(css = "[class*='elite-plan'], [class*='plan-card']:last-of-type")
    private WebElement elitePlanCard;

    @FindBy(css = "[class*='elite-plan'] button[class*='subscribe'], " +
            "[class*='plan-card']:last-of-type button")
    private WebElement eliteSubscribeBtn;

    @FindBy(css = "[class*='coming-soon'], [disabled], [aria-disabled='true']")
    private List<WebElement> comingSoonFeatures;

    @FindBy(css = "[class*='sp-balance'], [class*='skill-points']")
    private WebElement spBalance;

    @FindBy(css = "[class*='payment-modal'], [class*='payment-form'], [class*='checkout']")
    private WebElement paymentFlow;

    public SubscriptionsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isProPlanVisible() { return isDisplayed(proPlanCard); }
    public boolean isElitePlanVisible() { return isDisplayed(elitePlanCard); }

    public String getProPlanText() {
        try { return proPlanCard.getText(); } catch (Exception e) { return ""; }
    }

    public String getElitePlanText() {
        try { return elitePlanCard.getText(); } catch (Exception e) { return ""; }
    }

    public void clickProSubscribe() {
        click(proSubscribeBtn, "Pro Subscribe button");
    }

    public void clickEliteSubscribe() {
        click(eliteSubscribeBtn, "Elite Subscribe button");
    }

    public boolean isPaymentFlowInitiated() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(paymentFlow)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean areComingSoonFeaturesDisabled() {
        return comingSoonFeatures.stream().allMatch(el -> {
            String disabled = el.getAttribute("disabled");
            String ariaDisabled = el.getAttribute("aria-disabled");
            return "true".equals(disabled) || "true".equals(ariaDisabled) || disabled != null;
        });
    }

    public String getSpBalanceText() {
        try { return spBalance.getText().trim(); } catch (Exception e) { return ""; }
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
