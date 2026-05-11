package com.cts.mfrp.skillbarter.utils;

import com.cts.mfrp.skillbarter.constants.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads key-value pairs from config.properties.
 * Falls back to AppConstants defaults when a key is absent.
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(AppConstants.CONFIG_PATH)) {
            props.load(fis);
            log.info("Configuration loaded from: {}", AppConstants.CONFIG_PATH);
        } catch (IOException e) {
            log.warn("config.properties not found – using built-in defaults. ({})", e.getMessage());
        }
    }

    private ConfigReader() {}

    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static String getBaseUrl() {
        String url = props.getProperty("base.url");
        return (url != null && !url.isBlank()) ? url : AppConstants.BASE_URL;
    }

    public static String getBrowser() {
        String browser = props.getProperty("browser");
        return (browser != null && !browser.isBlank()) ? browser : AppConstants.BROWSER_CHROME;
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(props.getProperty("headless", "false"));
    }

    public static String getValidEmail() {
        String e = props.getProperty("valid.email");
        return (e != null && !e.isBlank()) ? e : AppConstants.VALID_EMAIL;
    }

    public static String getValidPassword() {
        String p = props.getProperty("valid.password");
        return (p != null && !p.isBlank()) ? p : AppConstants.VALID_PASSWORD;
    }
}
