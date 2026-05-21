package com.cts.mfrp.skillbarter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Retries a flaky test up to MAX_RETRY times before letting it land as
 * pass/fail/skip.
 *
 * Important: we DON'T retry on
 *   - SkipException     (intentional skips like "no pending session request" —
 *                       retrying just produces 'Ignored' entries in the report)
 *   - AssertionError    (a failed assertion will fail the exact same way the
 *                       next two times — pure noise, including for bug-flag
 *                       tests where the failure IS the result)
 * We only retry on real Selenium flake (TimeoutException, ElementNotInteractable,
 * StaleElementReference, etc) — those can legitimately succeed on a re-run.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount >= MAX_RETRY) return false;

        Throwable t = result.getThrowable();
        if (t == null) return false;

        // Intentional skips — pass straight through, don't pollute the report.
        if (t instanceof SkipException) return false;

        // Failed assertions are a final outcome, not transient flake.
        if (t instanceof AssertionError) return false;

        retryCount++;
        log.warn("🔄 Retrying '{}' [{}/{}] after {}",
                result.getMethod().getMethodName(), retryCount, MAX_RETRY,
                t.getClass().getSimpleName());
        return true;
    }
}
