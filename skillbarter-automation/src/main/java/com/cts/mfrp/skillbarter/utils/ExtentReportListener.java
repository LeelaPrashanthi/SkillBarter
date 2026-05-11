package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * TestNG ITestListener that writes pass/fail/skip results to ExtentReports
 * and captures screenshots on failure.
 */
public class ExtentReportListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(ExtentReportListener.class);
    private static final ExtentReports extent = ExtentManager.getInstance();

    // ThreadLocal so parallel tests get their own ExtentTest node
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String[] groups = result.getMethod().getGroups();

        ExtentTest test = extent.createTest(testName, description);
        for (String g : groups) test.assignCategory(g);
        testThread.set(test);
        log.info("▶ STARTED : {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        testThread.get().log(Status.PASS, "✅ Test PASSED");
        log.info("✅ PASSED  : {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testThread.get();
        test.log(Status.FAIL, "❌ Test FAILED: " + result.getThrowable());

        // Capture screenshot
        try {
            WebDriver driver = (WebDriver) result.getTestClass()
                    .getRealClass().getField("driver")
                    .get(result.getInstance());

            if (driver != null) {
                String ssPath = captureScreenshot(driver, result.getMethod().getMethodName());
                test.fail("Screenshot",
                        MediaEntityBuilder.createScreenCaptureFromPath(ssPath).build());
            }
        } catch (Exception e) {
            log.warn("Could not capture screenshot: {}", e.getMessage());
        }

        log.error("❌ FAILED  : {} | {}", result.getMethod().getMethodName(),
                result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        testThread.get().log(Status.SKIP, "⏭ Test SKIPPED: " + result.getThrowable());
        log.warn("⏭ SKIPPED  : {}", result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        log.info("📄 Extent Report flushed → {}", AppConstants.REPORTS_PATH);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    public static ExtentTest getTest() {
        return testThread.get();
    }

    private String captureScreenshot(WebDriver driver, String methodName) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = methodName + "_" + timestamp + ".png";
        Path ssDir  = Paths.get(AppConstants.SCREENSHOTS_PATH);
        Files.createDirectories(ssDir);
        Path ssPath = ssDir.resolve(fileName);

        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(ssPath, bytes);
        return ssPath.toAbsolutePath().toString();
    }
}
