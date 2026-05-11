package com.cts.mfrp.skillbarter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failing test up to MAX_RETRY times before marking it as failed.
 * Attach via @Test(retryAnalyzer = RetryAnalyzer.class) or globally via testng.xml listener.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("🔄 Retrying '{}' [{}/{}]",
                    result.getMethod().getMethodName(), retryCount, MAX_RETRY);
            return true;
        }
        return false;
    }
}
