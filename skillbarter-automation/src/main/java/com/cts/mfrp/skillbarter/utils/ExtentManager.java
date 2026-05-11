package com.cts.mfrp.skillbarter.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.cts.mfrp.skillbarter.constants.AppConstants;

/**
 * Singleton factory for the ExtentReports instance.
 * Call ExtentManager.getInstance() from the listener to get a shared reporter.
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
        ExtentSparkReporter spark = new ExtentSparkReporter(AppConstants.REPORTS_PATH);
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
        return er;
    }
}
