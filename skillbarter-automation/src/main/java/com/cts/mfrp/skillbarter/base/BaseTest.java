package com.cts.mfrp.skillbarter.base;

import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.utils.ConfigReader;
import com.cts.mfrp.skillbarter.utils.ExtentReportListener;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.annotations.Listeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.time.Duration;

/**
 * Base test class – all test classes extend this.
 * Handles driver lifecycle, waits, and common navigation helpers.
 *
 * Thread-safety: driver is stored in a ThreadLocal so parallel test runs
 * each get their own isolated browser instance.
 *
 * The ExtentReportListener is attached here so reports are also generated
 * when running a single test method from the IDE (which bypasses testng.xml).
 */
@Listeners(ExtentReportListener.class)
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    // ThreadLocal driver supports parallel execution
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Exposed as instance field so ExtentReportListener can access via reflection
    public WebDriver driver;

    protected WebDriverWait wait;

    // ── Driver Lifecycle ──────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional String browser) {
        String resolvedBrowser = (browser != null && !browser.isBlank())
                ? browser : ConfigReader.getBrowser();
        boolean headless = ConfigReader.isHeadless();

        WebDriver wd = createDriver(resolvedBrowser, headless);
        wd.manage().window().maximize();
        wd.manage().timeouts().implicitlyWait(Duration.ofSeconds(AppConstants.IMPLICIT_WAIT));
        wd.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(AppConstants.PAGE_LOAD_WAIT));
        wd.manage().timeouts().scriptTimeout(Duration.ofSeconds(AppConstants.SCRIPT_WAIT));

        driverThreadLocal.set(wd);
        driver = wd;
        wait = new WebDriverWait(wd, Duration.ofSeconds(AppConstants.EXPLICIT_WAIT));

        log.info("Browser '{}' launched (headless={})", resolvedBrowser, headless);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver wd = driverThreadLocal.get();
        if (wd != null) {
            wd.quit();
            driverThreadLocal.remove();
            driver = null;
            log.info("Browser closed.");
        }
    }

    // ── Static accessor for ThreadLocal driver ────────────────────────────────
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    protected void navigateTo(String url) {
        driver.get(url);
        log.debug("Navigated to: {}", url);
    }

    protected void navigateToBase() {
        navigateTo(ConfigReader.getBaseUrl());
    }

    protected void navigateToSignIn() {
        navigateTo(AppConstants.SIGNIN_URL);
    }

    protected void navigateToDashboard() {
        navigateTo(AppConstants.DASHBOARD_URL);
    }
    protected void navigateToMatches() {
        navigateTo(AppConstants.MATCHES_URL);
    }
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    // ── JS helpers ────────────────────────────────────────────────────────────

    protected Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected void scrollToBottom() {
        executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    protected void scrollToTop() {
        executeScript("window.scrollTo(0, 0);");
    }

    // ── Driver factory ────────────────────────────────────────────────────────

    private WebDriver createDriver(String browser, boolean headless) {
        String b = browser.toLowerCase();
        if (AppConstants.BROWSER_FIREFOX.equals(b)) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions opts = new FirefoxOptions();
            if (headless) opts.addArguments("--headless");
            return new FirefoxDriver(opts);
        }
        if (AppConstants.BROWSER_EDGE.equals(b)) {
            WebDriverManager.edgedriver().setup();
            EdgeOptions opts = new EdgeOptions();
            if (headless) opts.addArguments("--headless");
            return new EdgeDriver(opts);
        }
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (headless) opts.addArguments("--headless=new");
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage",
                "--disable-gpu", "--window-size=1920,1080");
        return new ChromeDriver(opts);
    }
}
