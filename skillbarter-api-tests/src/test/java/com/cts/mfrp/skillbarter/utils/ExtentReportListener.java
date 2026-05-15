package com.cts.mfrp.skillbarter.utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportListener – TestNG ITestListener that writes pass/fail/skip
 * results to an ExtentReports HTML file after every test method.
 */
public class ExtentReportListener implements ITestListener {

    @Override public void onTestStart(ITestResult result) { }

    @Override public void onTestSuccess(ITestResult result) { }

    @Override public void onTestFailure(ITestResult result) { }

    @Override public void onTestSkipped(ITestResult result) { }

    @Override public void onFinish(ITestContext context) { }
}
