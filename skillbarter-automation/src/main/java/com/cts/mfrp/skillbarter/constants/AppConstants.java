package com.cts.mfrp.skillbarter.constants;

/**
 * Application-wide constants for SkillBarter automation.
 */
public class AppConstants {

    private AppConstants() {}

    // ── URLs ────────────────────────────────────────────────────────────────
    public static final String BASE_URL              = "https://skill-barter-wine-theta.vercel.app/";
    public static final String SIGNIN_URL            = BASE_URL + "login";
    public static final String SIGNUP_URL            = BASE_URL + "signup";
    public static final String DASHBOARD_URL         = BASE_URL + "app/dashboard";
    public static final String PROFILE_CREATION_URL  = BASE_URL + "profile-creation";
    public static final String PROFILE_SETUP_URL     = BASE_URL + "profile-setup";
    public static final String MATCHES_URL           = BASE_URL + "app/matches";
    public static final String MESSENGER_URL         = BASE_URL + "app/chat";
    public static final String CALENDAR_URL          = BASE_URL + "app/calendar";
    public static final String PROGRESS_URL          = BASE_URL + "progress";
    public static final String COMMUNITY_URL         = BASE_URL + "app/community";
    public static final String SUBSCRIPTIONS_URL     = BASE_URL + "subscriptions";
    public static final String SAVED_PROFILES_URL    = BASE_URL + "app/saved-profiles";
    public static final String USER_PROFILE_URL      = BASE_URL + "profile";

    // ── Timeouts ─────────────────────────────────────────────────────────────
    public static final int IMPLICIT_WAIT   = 10;   // seconds
    public static final int EXPLICIT_WAIT   = 15;   // seconds
    public static final int PAGE_LOAD_WAIT  = 30;   // seconds
    public static final int SCRIPT_WAIT     = 30;   // seconds

    // ── Test Data ────────────────────────────────────────────────────────────
    public static final String TEST_DATA_PATH  = "src/test/resources/testdata/TestData.xlsx";
    public static final String CONFIG_PATH     = "src/test/resources/config.properties";

    // ── Report & Screenshot Paths ─────────────────────────────────────────
    public static final String REPORTS_PATH      = "test-output/reports/ExtentReport.html";
    public static final String SCREENSHOTS_PATH  = "test-output/screenshots/";

    // ── Sheet Names ───────────────────────────────────────────────────────────
    public static final String SHEET_LOGIN     = "LoginData";
    public static final String SHEET_SIGNUP    = "SignUpData";
    public static final String SHEET_PROFILE   = "ProfileData";
    public static final String SHEET_MATCHES        = "MatchesData";
    public static final String SHEET_MATCHESSEARCH  = "MatchesSearchData";
    public static final String SHEET_COMMUNITY = "CommunityData";

    // ── Valid Test Credentials ────────────────────────────────────────────────
    public static final String VALID_EMAIL    = "spidy@gmail.com";
    public static final String VALID_PASSWORD = "spidy@1234";

    // ── Titles & Headings ────────────────────────────────────────────────────
    public static final String DASHBOARD_TITLE    = "Dashboard";
    public static final String SIGNIN_HEADING     = "Welcome to SkillSwap! Sign In";
    public static final String SIGNUP_HEADING     = "Welcome to SkillSwap! Sign Up";
    public static final String PROFILE_HEADING    = "Set Up Your Profile";
    public static final String COMMUNITY_TITLE    = "Community";
    public static final String CALENDAR_TITLE     = "Calendar";
    public static final String PROGRESS_TITLE     = "Progress";
    public static final String SUBSCRIPTIONS_TITLE = "SkillBarter+ Subscriptions";

    // ── Error Messages ────────────────────────────────────────────────────────
    public static final String ERR_INVALID_CREDENTIALS  = "Invalid email or password.";
    public static final String ERR_EMAIL_EXISTS          = "Email address already registered.";
    public static final String ERR_PASSWORD_MISMATCH     = "Passwords do not match.";
    public static final String ERR_REQUIRED_FIELDS       = "Please fill all required fields.";
    public static final String ERR_WEAK_PASSWORD         = "Password does not meet security criteria";
    public static final String ERR_COMPLETE_FIELDS       = "Please complete all required fields.";

    // ── Success Messages ─────────────────────────────────────────────────────
    public static final String MSG_ACCOUNT_CREATED  = "Account created successfully. Please sign in.";
    public static final String MSG_PROFILE_CREATED  = "Profile created successfully.";

    // ── Browser Config ────────────────────────────────────────────────────────
    public static final String BROWSER_CHROME  = "chrome";
    public static final String BROWSER_FIREFOX = "firefox";
    public static final String BROWSER_EDGE    = "edge";
}
