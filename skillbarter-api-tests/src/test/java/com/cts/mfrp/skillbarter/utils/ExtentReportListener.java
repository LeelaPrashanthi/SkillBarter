package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportListener – TestNG ITestListener that writes pass/fail/skip
 * results to an ExtentReports HTML file after every test method.
 */
public class ExtentReportListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentTest test = ExtentReportManager.getInstance()
                .createTest(name, desc == null ? "" : desc);
        ExtentReportManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) {
            Throwable err = result.getThrowable();
            t.log(Status.FAIL, err == null ? "Test failed" : err.toString());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) {
            Throwable err = result.getThrowable();
            t.log(Status.SKIP, err == null ? "Test skipped" : err.getMessage());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
    }
}
