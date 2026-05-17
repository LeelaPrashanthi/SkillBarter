package com.cts.mfrp.skillbarter.utils;

/**
 * Bootstrap – pre-test data seeding helpers.
 *
 * Each ensure* method is idempotent: it registers / logs in the required
 * user only once and stores the resulting token + ID in TestContext.
 * Call these from @BeforeClass in any test class that needs seeded data.
 */
public class Bootstrap {

    public static void ensureFirstUser() { }

    public static void ensureSecondUser() { }

    public static void ensureSkill() { }

    public static void ensureMatch() { }

    public static void ensureSession() { }

    public static void ensureMessage() { }

    public static void ensureNotification() { }

    public static void ensureReview() { }

    public static void ensureCalendarEvent() { }

    public static void ensureTransaction() { }

    public static void ensureStory() { }

    private Bootstrap() { }
}
