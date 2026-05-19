package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportListener – TestNG ITestListener that writes pass/fail/skip
 * results to an ExtentReports HTML file at test-output/reports/ApiReport.html.
 *
 * Registered in src/test/resources/suites/testng.xml so every suite run
 * produces a fresh HTML report without any test-class changes.
 */
public class ExtentReportListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        // Build the singleton up-front so the report file exists from the first test.
        ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        // One Extent node per test method – name = method, description = @Test(description=...)
        String name = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        ExtentTest test = ExtentReportManager.getInstance()
                .createTest(name, description == null ? "" : description);
        for (String group : result.getMethod().getGroups()) test.assignCategory(group);
        ExtentReportManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) test.log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;
        Throwable t = result.getThrowable();
        test.log(Status.FAIL, t == null ? "Test failed" : t.toString());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;
        Throwable t = result.getThrowable();
        test.log(Status.SKIP, t == null ? "Test skipped" : t.toString());
    }

    @Override
    public void onFinish(ITestContext context) {
        // Flush once at the very end – this is what actually writes the HTML to disk.
        ExtentReportManager.flush();
    }
}
