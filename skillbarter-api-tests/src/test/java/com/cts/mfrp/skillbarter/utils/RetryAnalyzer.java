package com.cts.mfrp.skillbarter.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer – retries a failing test up to MAX_RETRY times.
 * Attach via retryAnalyzer = RetryAnalyzer.class on each @Test.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = 2;

    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (count >= MAX_RETRY) return false;

        Throwable t = result.getThrowable();
        if (t == null) return false;

        if (t instanceof AssertionError) return false;

        count++;
        return true;
    }
}
