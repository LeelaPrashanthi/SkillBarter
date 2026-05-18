package com.cts.mfrp.skillbarter.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer – retries a failing test up to MAX_RETRY times.
 * Attach via retryAnalyzer = RetryAnalyzer.class on each @Test.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = 2;
    private int attempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < MAX_RETRY) {
            attempts++;
            return true;
        }
        return false;
    }
}
