package com.cts.mfrp.skillbarter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader – loads key-value pairs from config.properties
 * and exposes typed getters for the framework.
 */
public class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String getBaseUrl() { return PROPS.getProperty("base.url"); }

    public static String getTestEmail() { return PROPS.getProperty("test.email"); }

    public static String getTestPassword() { return PROPS.getProperty("test.password"); }

    public static String getSecondUserEmail() { return PROPS.getProperty("second.user.email"); }

    public static String getSecondUserPassword() { return PROPS.getProperty("second.user.password"); }

    public static int getConnectionTimeout() {
        return Integer.parseInt(PROPS.getProperty("connection.timeout", "10000"));
    }

    public static int getReadTimeout() {
        return Integer.parseInt(PROPS.getProperty("read.timeout", "15000"));
    }

    public static String get(String key) { return PROPS.getProperty(key); }

    private ConfigReader() { }
}
