package com.cts.mfrp.skillbarter.base;

import com.cts.mfrp.skillbarter.utils.ConfigReader;
import io.restassured.RestAssured;
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
    public void initSpec() {
        String baseUrl = ConfigReader.getBaseUrl();
        RestAssured.baseURI = baseUrl;

        // Trust all certs — needed for self-signed / corporate-proxy intercepted HTTPS.
        RestAssured.useRelaxedHTTPSValidation();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();

        log.info("API base URL: {}", baseUrl);
    }

    /** Unauthenticated spec — for register / login / public endpoints. */
    protected RequestSpecification spec() {
        if (requestSpec == null) initSpec();
        return RestAssured.given().spec(requestSpec);
    }

    /** Authenticated spec — adds Bearer token header. */
    protected RequestSpecification authSpec(String token) {
        return spec().header("Authorization", "Bearer " + token);
    }

    protected String getBaseUrl() { return ConfigReader.getBaseUrl(); }
}
