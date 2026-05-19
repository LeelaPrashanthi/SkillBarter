package com.cts.mfrp.skillbarter.tests;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.constants.AppConstants;
import com.cts.mfrp.skillbarter.pages.ProfileCreationPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * Test class for Profile Creation page.
 * Test Cases : TC_021 → TC_024
 */
public class ProfileCreationTest extends BaseTest {

    private ProfileCreationPage profilePage;

    @BeforeMethod(alwaysRun = true)
    public void openProfileCreationPage() {
        // 1. Direct-form login (same pattern as CalendarTest) — no homepage
        //    CTA probe-click, which was brittle to button label/class churn.
        navigateTo(AppConstants.SIGNIN_URL);

        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement email = loginWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email']")));
        WebElement pwd = driver.findElement(By.xpath("//input[@type='password']"));

        email.clear();
        email.sendKeys(AppConstants.VALID_EMAIL);
        pwd.clear();
        pwd.sendKeys(AppConstants.VALID_PASSWORD);
        pwd.sendKeys(Keys.RETURN);

        loginWait.until(ExpectedConditions.urlContains("dashboard"));

        // 2. Open the topbar user dropdown — wait for it to be clickable
        //    rather than racing it with a fixed sleep.
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        pageWait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//header//*[contains(@class,'user-name') or contains(@class,'username') "
                + "or contains(@class,'user-profile') or contains(@class,'profile-pic') "
                + "or contains(@class,'avatar') or (self::button and contains(@class,'user'))] "
                + "| //nav//*[contains(@class,'user-name') or contains(@class,'avatar') "
                + "or (self::button and contains(@class,'user'))] "
                + "| //*[contains(@class,'topbar') or contains(@class,'navbar')]"
                + "//*[contains(@class,'user')]"))).click();

        // 3. Click "Profile" / "Go to Profile" inside the open dropdown — lands on /app/profile.
        clickProfileOptionInDropdown();

        // 4. Wait for the profile form to render (Name field anchors the page).
        pageWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/label[contains(text(),'Name')]/following::input[1]")));

        profilePage = new ProfileCreationPage(driver);
    }

    // ── TESTS ────────────────────────────────────────────────────────────────

    @Test(testName = "TC_021", description = "Create profile with valid data and save")
    public void tc021_createProfileWithValidData() {
        profilePage.enterName("John Tester");
        profilePage.enterDescription("Passionate learner exchanging coding for design skills.");

        profilePage.clickSkillsToTeachDropdown();
        selectFirstDropdownOption();

        profilePage.clickSkillsToLearnDropdown();
        selectFirstDropdownOption();

        profilePage.clickContinue();

        String url = getCurrentUrl();
        Assert.assertTrue(
            url.contains("dashboard") || !profilePage.getSuccessMessage().isEmpty(),
            "Expected redirect to dashboard or success message. URL: " + url
        );
    }

    @Test(testName = "TC_022", description = "Empty mandatory fields show validation error or block submission")
    public void tc022_missingMandatoryFieldsShowError() {
        String urlBefore = getCurrentUrl();

        // Mark required fields as touched (type-then-clear) so Angular fires validation.
        touchAndClear(By.xpath("//div/label[contains(text(),'Name')]/following::input[1]"));
        touchAndClear(By.xpath("//div/label[text()='Profile Description']/following::textarea"));

        profilePage.clickContinue();

        // Wait up to 5s for EITHER a validation error to render OR the URL to
        // change. Whichever happens first satisfies the assertion below.
        By errorLocator = By.xpath(
                "//*[contains(@class,'error') or contains(@class,'mat-error') "
                + "or contains(@class,'alert-danger') or contains(@class,'invalid-feedback') "
                + "or contains(@class,'validation') or @role='alert' or @aria-invalid='true' "
                + "or contains(translate(normalize-space(.),'REQUIRD','required'),'required')]");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> !d.getCurrentUrl().equals(urlBefore)
                             || d.findElements(errorLocator).stream().anyMatch(e -> {
                                 try { return e.isDisplayed() && !e.getText().trim().isEmpty(); }
                                 catch (Exception ignored) { return false; }
                             }));
        } catch (Exception ignored) {
            // Timeout — assertions below still run and will fail with detail.
        }

        boolean errorVisible = driver.findElements(errorLocator)
                .stream().anyMatch(e -> {
                    try { return e.isDisplayed() && !e.getText().trim().isEmpty(); }
                    catch (Exception ignored) { return false; }
                });

        boolean stillOnForm = getCurrentUrl().equals(urlBefore);

        Assert.assertTrue(
            errorVisible || stillOnForm,
            "Expected validation error or form to refuse submission, but page navigated to: " + getCurrentUrl()
        );
    }

    @Test(testName = "TC_023", description = "Avatar upload attaches file to the input")
    public void tc023_avatarUploadUpdatesPreview() {
        // Skip rather than fail when the avatar file isn't on this machine.
        // The original path was hardcoded to a different user's profile —
        // this keeps the suite green on any developer's box.
        Path avatarFile = Paths.get("C:\\Users\\2480084\\OneDrive - Cognizant\\Pictures\\OIP.webp");
        if (!Files.isRegularFile(avatarFile)) {
            throw new org.testng.SkipException(
                "Avatar file not present at " + avatarFile + " — skipping TC_023. "
                + "Drop any small image at that path (or update tc023) to enable this test.");
        }
        String avatarPath = avatarFile.toAbsolutePath().toString();

        List<WebElement> fileInputs = driver.findElements(By.xpath("//input[@type='file']"));
        if (fileInputs.isEmpty()) {
            throw new RuntimeException("No <input type='file'> found. URL: " + getCurrentUrl());
        }
        WebElement fileInput = fileInputs.get(0);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "arguments[0].style.display='block'; arguments[0].style.visibility='visible'; "
                + "arguments[0].style.opacity='1'; arguments[0].style.width='1px'; arguments[0].style.height='1px';",
                fileInput);
        fileInput.sendKeys(avatarPath);

        // Wait up to 5s for the browser to attach the file to the input
        // (files.length > 0). Replaces a blind 2s sleep.
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> {
                    Long n = (Long) ((JavascriptExecutor) d).executeScript(
                            "return arguments[0].files ? arguments[0].files.length : 0;", fileInput);
                    return n != null && n > 0;
                });

        Long fileCount = (Long) js.executeScript("return arguments[0].files ? arguments[0].files.length : 0;", fileInput);
        String fileName = (String) js.executeScript(
                "return (arguments[0].files && arguments[0].files[0]) ? arguments[0].files[0].name : '';", fileInput);

        Assert.assertTrue(fileCount != null && fileCount > 0, "File was not attached after sendKeys.");
        Assert.assertTrue(fileName != null && fileName.toLowerCase().contains("oip"),
                "File attached but name doesn't match. Got: '" + fileName + "'");
    }

    @Test(testName = "TC_024", description = "Add 2 skills each to Teach and Learn dropdowns")
    public void tc024_multiSelectDropdowns() {
        addFirstAvailableSkill("Teach");
        addFirstAvailableSkill("Teach");
        addFirstAvailableSkill("Learn");
        addFirstAvailableSkill("Learn");

        Assert.assertFalse(
            profilePage.isErrorMessageDisplayed(),
            "Unexpected error displayed when selecting skills"
        );
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    /** Clicks the "Profile" / "Go to Profile" option inside the open dropdown. */
    private void clickProfileOptionInDropdown() {
        String tx = "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
        String exactMatch = "(" + tx + "='profile' or " + tx + "='go to profile' or " + tx + "='my profile')";

        List<WebElement> options = driver.findElements(By.xpath(
                "//*[contains(@class,'dropdown') or contains(@class,'menu') or contains(@class,'popover') "
                + "or contains(@class,'user-menu') or @role='menu']"
                + "//*[(self::a or self::button or self::li or self::div or self::span) and " + exactMatch + "]"));
        if (options.isEmpty()) {
            options = driver.findElements(By.xpath(
                    "//*[(self::a or self::button or self::li) and " + exactMatch + "]"));
        }

        WebElement target = null;
        for (WebElement opt : options) {
            try { if (opt.isDisplayed()) { target = opt; break; } } catch (Exception ignored) {}
        }
        if (target == null) {
            throw new RuntimeException("No 'Profile' option in dropdown. URL: " + getCurrentUrl());
        }

        // Walk up to the nearest clickable ancestor.
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement clickable = (WebElement) js.executeScript(
            "let el = arguments[0];" +
            "while (el && el !== document.body) {" +
            "  const tag = el.tagName.toLowerCase(); const role = el.getAttribute('role');" +
            "  if (tag === 'a' || tag === 'button' || tag === 'li' || role === 'menuitem') return el;" +
            "  el = el.parentElement;" +
            "} return arguments[0];", target);

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", clickable);

        String urlBefore = getCurrentUrl();
        try { clickable.click(); }
        catch (StaleElementReferenceException stale) { return; }
        catch (Exception ignored) {}

        // If URL didn't change, retry with Actions + JS click.
        if (urlBefore.equals(getCurrentUrl())) {
            try { new Actions(driver).moveToElement(clickable).click().perform(); }
            catch (StaleElementReferenceException stale) { return; }
            catch (Exception ignored) {}
        }
        if (urlBefore.equals(getCurrentUrl())) {
            try { js.executeScript("arguments[0].click();", clickable); }
            catch (StaleElementReferenceException stale) { /* landed */ }
        }
    }

    /** Types a char then clears it — marks Angular controls as dirty + touched. */
    private void touchAndClear(By locator) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            el.click();
            el.sendKeys("x");
            el.clear();
            el.sendKeys(Keys.TAB);
        } catch (Exception ignored) {}
    }

    /** Clicks the first visible custom-dropdown option after opening it (used by TC_021). */
    private void selectFirstDropdownOption() {
        By optionLocator = By.xpath(
                "//*[contains(@class,'dropdown-option') or contains(@class,'mat-option') "
                + "or contains(@class,'select-option') or @role='option' "
                + "or (self::li and (contains(@class,'option') or contains(@class,'item')))]");

        // Wait up to 5s for any option to render after the dropdown opens.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> d.findElements(optionLocator).stream().anyMatch(e -> {
                        try { return e.isDisplayed(); }
                        catch (Exception ignored) { return false; }
                    }));
        } catch (Exception ignored) {
            // Fall through — empty list will just be a no-op below.
        }

        for (WebElement opt : driver.findElements(optionLocator)) {
            try {
                if (opt.isDisplayed()) {
                    opt.click();
                    try { driver.findElement(By.tagName("body")).click(); } catch (Exception ignored) {}
                    return;
                }
            } catch (Exception ignored) {}
        }
    }

    /** Picks the first real (non-placeholder) option from the column's dropdown and clicks Add. */
    private void addFirstAvailableSkill(String labelKeyword) {
        String labelText = "Teach".equals(labelKeyword) ? "Skills I Have (Teach)" : "Skills I Need to Learn";
        By selectLocator = By.xpath("//div/label[text()='" + labelText + "']/parent::div/div[2]/select");

        WebDriverWait optsWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String chosenText;
        try {
            chosenText = optsWait.until(d -> {
                try {
                    WebElement sel = d.findElement(selectLocator);
                    for (WebElement opt : sel.findElements(By.tagName("option"))) {
                        String t = opt.getText().trim();
                        if (t.isEmpty()) continue;
                        String lt = t.toLowerCase();
                        if (lt.startsWith("select") || lt.startsWith("choose") || lt.contains("--")) continue;
                        return t;
                    }
                    return null;
                } catch (Exception e) { return null; }
            });
        } catch (Exception timeout) {
            throw new RuntimeException(
                "No selectable skill option in '" + labelKeyword + "' dropdown after 15s. URL: " + getCurrentUrl(),
                timeout);
        }
        addSkillToColumn(labelKeyword, chosenText);
    }

    /** Selects the option with the given text from the column's dropdown and clicks Add. */
    private void addSkillToColumn(String labelKeyword, String optionText) {
        String labelText = "Teach".equals(labelKeyword) ? "Skills I Have (Teach)" : "Skills I Need to Learn";
        String columnRoot = "//div/label[text()='" + labelText + "']/parent::div";

        By selectLocator = By.xpath(columnRoot + "/div[2]/select");
        WebElement selectEl = wait.until(ExpectedConditions.visibilityOfElementLocated(selectLocator));

        WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath(columnRoot + "/div[2]/select/option[normalize-space(.)='" + optionText + "']")));
        option.click();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));"
            + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            selectEl);

        // Wait for Add to enable, then click it.
        By addBtnLocator = By.xpath(columnRoot + "/div[2]/button[normalize-space()='Add']");
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement addBtn = longWait.until(d -> {
            try {
                WebElement b = d.findElement(addBtnLocator);
                if (!b.isDisplayed()) return null;
                String disabled = b.getAttribute("disabled");
                return (disabled == null || disabled.isEmpty() || "false".equalsIgnoreCase(disabled)) ? b : null;
            } catch (Exception e) { return null; }
        });

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", addBtn);
        try {
            new Actions(driver).moveToElement(addBtn).pause(150).click().perform();
        } catch (Exception e) {
            js.executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));",
                addBtn);
        }

        // Wait for the chip to appear OR the Add button to be disabled again (form reset).
        try {
            longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(columnRoot + "/div[1]//*[contains(normalize-space(.),'" + optionText + "')]")));
        } catch (Exception ignored) {}
    }
}
