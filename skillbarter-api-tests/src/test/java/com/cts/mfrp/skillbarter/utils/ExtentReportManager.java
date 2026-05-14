package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

/**
 * ExtentReportManager – singleton wrapper for ExtentReports HTML output.
 * Listener calls getInstance() once; test classes call getTest().
 */
public class ExtentReportManager {

    public static ExtentReports getInstance() { return null; }

    public static ExtentTest getTest() { return null; }

    public static void setTest(ExtentTest test) { }

    public static void flush() { }

    private ExtentReportManager() { }
}
