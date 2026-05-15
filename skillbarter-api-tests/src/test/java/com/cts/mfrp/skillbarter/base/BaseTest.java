package com.cts.mfrp.skillbarter.base;

import com.cts.mfrp.skillbarter.utils.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;

/**
 * BaseTest – parent of every API test class.
 *
 * Responsibilities:
 *  - Builds and exposes a shared RequestSpecification (base URL, content type, logging)
 *  - Provides a spec() helper so sub-classes never repeat boilerplate
 *  - Reads configuration from config.properties via ConfigReader
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    private static RequestSpecification requestSpec;

    @BeforeSuite(alwaysRun = true)
    public void initSpec() { }

    protected RequestSpecification spec() { return null; }

    protected RequestSpecification authSpec(String token) { return null; }

    protected String getBaseUrl() { return null; }
}
