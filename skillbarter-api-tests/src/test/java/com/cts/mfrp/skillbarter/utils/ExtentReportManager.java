package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * ExtentReportManager – singleton wrapper for ExtentReports HTML output.
 *
 * The HTML report is written to test-output/reports/ApiReport.html (path is
 * overridable via the `reports.path` key in config.properties; falls back to
 * the default if ConfigReader is not wired yet).
 *
 * Lifecycle:
 *   1. ExtentReportListener.onStart  -> getInstance() lazily creates the
 *      ExtentReports object and attaches a SparkReporter.
 *   2. onTestStart                   -> setTest(...) stores the per-thread ExtentTest.
 *   3. onTestSuccess/Failure/Skipped -> getTest() retrieves it and logs status.
 *   4. onFinish                      -> flush() writes the HTML to disk.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    /** Returns the singleton ExtentReports instance, building it on first access. */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            // Resolve report path. ConfigReader is stubbed today, so guard against null.
            String configured = safe(ConfigReader.get("reports.path"));
            String path = (configured == null || configured.isBlank())
                    ? "test-output/reports/ApiReport.html"
                    : configured;

            // Ensure the parent folder exists – Spark will not create it for us.
            File reportFile = new File(path);
            File parent = reportFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportFile);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("SkillBarter API Test Report");
            spark.config().setReportName("SkillBarter REST Assured Suite");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Project",     "skillbarter-api-tests");
            extent.setSystemInfo("Backend",     "https://skillbarter-ropl.onrender.com");
            extent.setSystemInfo("Framework",   "REST Assured + TestNG");
            extent.setSystemInfo("Java",        System.getProperty("java.version"));
            extent.setSystemInfo("OS",          System.getProperty("os.name"));
        }
        return extent;
    }

    /** Per-thread ExtentTest accessor – populated by the listener at onTestStart. */
    public static ExtentTest getTest() {
        return currentTest.get();
    }

    public static void setTest(ExtentTest test) {
        currentTest.set(test);
    }

    /** Writes everything buffered to the HTML file. Call once from the listener's onFinish. */
    public static void flush() {
        if (extent != null) extent.flush();
        currentTest.remove();
    }

    /** ConfigReader.get(...) returns null today – this guard makes us null-safe. */
    private static String safe(String value) {
        return value;
    }

    private ExtentReportManager() { }
}
