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
        if (requestSpec != null) return;

        RestAssured.baseURI = ConfigReader.getBaseUrl();
        RestAssured.useRelaxedHTTPSValidation();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    protected RequestSpecification spec() {
        if (requestSpec == null) initSpec();
        return new RequestSpecBuilder().addRequestSpecification(requestSpec).build();
    }

    protected RequestSpecification authSpec(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(spec())
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    protected String getBaseUrl() { return ConfigReader.getBaseUrl(); }
}
