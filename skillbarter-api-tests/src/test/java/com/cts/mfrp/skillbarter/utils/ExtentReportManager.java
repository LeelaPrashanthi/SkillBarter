package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.File;

/**
 * ExtentReportManager – singleton wrapper for ExtentReports HTML output.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String path = ConfigReader.get("reports.path");
            if (path == null || path.isEmpty()) {
                path = "test-output/reports/ApiReport.html";
            }
            File outFile = new File(path);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            ExtentSparkReporter spark = new ExtentSparkReporter(outFile);
            spark.config().setReportName("SkillBarter API Test Report");
            spark.config().setDocumentTitle("SkillBarter API Tests");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Base URL", ConfigReader.getBaseUrl());
            extent.setSystemInfo("Framework", "REST Assured + TestNG");
        }
        return extent;
    }

    public static ExtentTest getTest() { return CURRENT.get(); }

    public static void setTest(ExtentTest test) { CURRENT.set(test); }

    public static void flush() {
        if (extent != null) extent.flush();
    }

    private ExtentReportManager() { }
}
