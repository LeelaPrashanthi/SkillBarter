package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.cts.mfrp.skillbarter.constants.AppConstants;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton factory for the ExtentReports instance.
 * Call ExtentManager.getInstance() from the listener to get a shared reporter.
 *
 * Each suite run produces a timestamped file inside the reports folder so
 * prior reports are not overwritten.
 */
public class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            extent = createInstance();
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        String reportPath = resolveTimestampedReportPath();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("SkillBarter Automation Report");
        spark.config().setReportName("SkillBarter – Functional Test Results");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        ExtentReports er = new ExtentReports();
        er.attachReporter(spark);
        er.setSystemInfo("Application",  "SkillBarter");
        er.setSystemInfo("Environment",  ConfigReader.getBaseUrl());
        er.setSystemInfo("Browser",      ConfigReader.getBrowser());
        er.setSystemInfo("Author",       "CTS MFRP QA Team");
        er.setSystemInfo("Framework",    "Selenium 4 + TestNG 7 + POM");
        er.setSystemInfo("ReportFile",   reportPath);
        return er;
    }

    /** Returns "<reports-folder>/ExtentReport_<yyyy-MM-dd_HH-mm-ss>.html". */
    private static String resolveTimestampedReportPath() {
        File configured = new File(AppConstants.REPORTS_PATH);
        File reportDir = configured.getParentFile() != null
                ? configured.getParentFile()
                : new File("test-output/reports");
        if (!reportDir.exists()) reportDir.mkdirs();

        String stamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return new File(reportDir, "ExtentReport_" + stamp + ".html").getPath();
    }
}
