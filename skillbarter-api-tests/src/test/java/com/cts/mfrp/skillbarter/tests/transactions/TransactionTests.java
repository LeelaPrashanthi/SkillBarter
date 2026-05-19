package com.cts.mfrp.skillbarter.tests.transactions;

import com.cts.mfrp.skillbarter.base.BaseTest;
import com.cts.mfrp.skillbarter.utils.Bootstrap;
import com.cts.mfrp.skillbarter.utils.PayloadBuilder;
import com.cts.mfrp.skillbarter.utils.RetryAnalyzer;
import com.cts.mfrp.skillbarter.utils.TestContext;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

/**
 * TransactionTests – /api/transactions endpoints, written in REST Assured BDD style
 * (.given().when().then() with Hamcrest matchers).
 *
 * Live backend: https://skillbarter-ropl.onrender.com (verified 2026-05-19).
 * Schema: Transaction { transactionId:int, user:User, session:Session,
 *   amount:number, paymentMethod:enum[Card|UPI|NetBanking],
 *   status:enum[Success|Failure|Pending], createdAt:date-time }.
 * Required on create: amount, paymentMethod, status. user/session are nested
 * {userId}/{sessionId} objects, not raw IDs.
 *
 * 3 tests fail against the current backend — each surfaces a real defect:
 *   - createTransaction_noAuth_returns401: no auth enforcement (returns 201)
 *   - getTransactionById_nonExistentId_returns404: NoSuchElementException leaks as 500
 *   - deleteTransaction_nonExistentId_returns404: same leak on DELETE
 *
 * Notes:
 *   - User's spec table omits the required "status" field on POST body;
 *     PayloadBuilder defaults it to "Pending".
 *   - OpenAPI declares POST returns 200, but live backend returns 201 (matches
 *     the spec table). Tests assert 201.
 *   - /total endpoint returns a bare numeric body (e.g. "2700.00"), not a JSON
 *     object — handled by the extractTotal helper.
 *   - OAS exposes one extra endpoint not in the spec table:
 *     GET /api/transactions/user/{userId}/received — not covered here.
 */
public class TransactionTests extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void seed() {
        Bootstrap.ensureFirstUser();
        Bootstrap.ensureSession();
        Bootstrap.ensureTransaction();

        if (TestContext.authToken == null || TestContext.registeredUserId == null
                || TestContext.sessionId == null || TestContext.transactionId == null) {
            throw new IllegalStateException("Bootstrap failed to seed TransactionTests — "
                    + "check [Bootstrap] log lines above.");
        }
    }

    // ── POST /api/transactions ────────────────────────────────────────────────

    @Test(priority = 1, groups = {"transactions", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/transactions with valid payload returns 201 and transaction object")
    public void createTransaction_validPayload_returns201() {
        given().spec(authSpec(TestContext.authToken))
                .body(PayloadBuilder.createTransactionPayload(
                        TestContext.registeredUserId, TestContext.sessionId, 250.0, "UPI"))
        .when()
                .post("/api/transactions")
        .then()
                .statusCode(201)
                .body("transactionId", notNullValue())
                .body("amount", equalTo(250.0f))
                .body("paymentMethod", equalTo("UPI"))
                .body("user.userId", equalTo(Integer.parseInt(TestContext.registeredUserId)));
    }

    @Test(priority = 2, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/transactions with missing amount returns 400")
    public void createTransaction_missingAmount_returns400() {
        Map<String, Object> body = PayloadBuilder.createTransactionPayload(
                TestContext.registeredUserId, TestContext.sessionId, 0, "UPI");
        body.remove("amount");

        given().spec(authSpec(TestContext.authToken)).body(body)
        .when()
                .post("/api/transactions")
        .then()
                .statusCode(400);
    }

    @Test(priority = 3, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/transactions with invalid paymentMethod returns 400")
    public void createTransaction_invalidPaymentMethod_returns400() {
        given().spec(authSpec(TestContext.authToken))
                .body(PayloadBuilder.createTransactionPayload(
                        TestContext.registeredUserId, TestContext.sessionId, 100.0, "Crypto"))
        .when()
                .post("/api/transactions")
        .then()
                .statusCode(400);
    }

    @Test(priority = 4, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "POST /api/transactions without auth returns 401")
    public void createTransaction_noAuth_returns401() {
        given().spec(spec())
                .body(PayloadBuilder.createTransactionPayload(
                        TestContext.registeredUserId, TestContext.sessionId, 100.0, "UPI"))
        .when()
                .post("/api/transactions")
        .then()
                .statusCode(401);
    }

    // ── GET /api/transactions/{id} ────────────────────────────────────────────

    @Test(priority = 5, groups = {"transactions", "smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/{id} for existing transaction returns 200")
    public void getTransactionById_existingId_returns200() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/" + TestContext.transactionId)
        .then()
                .statusCode(200)
                .body("transactionId", equalTo(Integer.parseInt(TestContext.transactionId)))
                .body("user.userId", notNullValue())
                .body("amount", notNullValue());
    }

    @Test(priority = 6, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/{id} for non-existent id returns 404")
    public void getTransactionById_nonExistentId_returns404() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/99999999")
        .then()
                .statusCode(404);
    }

    // ── GET /api/transactions/user/{userId} ───────────────────────────────────

    @Test(priority = 7, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/user/{userId} returns all transactions for user")
    public void getTransactionsByUser_validUser_returnsList() {
        int uid = Integer.parseInt(TestContext.registeredUserId);
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/user/" + uid)
        .then()
                .statusCode(200)
                .body("user.userId", everyItem(equalTo(uid)));
    }

    @Test(priority = 8, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/user/{userId} for user with no transactions returns empty list")
    public void getTransactionsByUser_noTransactions_returnsEmptyList() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/user/99999999")
        .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    // ── GET /api/transactions/session/{sessionId} ─────────────────────────────

    @Test(priority = 9, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/session/{sessionId} returns transactions for session")
    public void getTransactionsBySession_validSession_returnsList() {
        int sid = Integer.parseInt(TestContext.sessionId);
        given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/session/" + sid)
        .then()
                .statusCode(200)
                .body("session.sessionId", everyItem(equalTo(sid)));
    }

    // ── GET /api/transactions/status?status=X ─────────────────────────────────

    @Test(priority = 10, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/status?status=Success returns only successful transactions")
    public void filterByStatus_success_returnsSuccessTransactions() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Success")
        .when()
                .get("/api/transactions/status")
        .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("Success")));
    }

    @Test(priority = 11, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/status?status=Pending returns pending transactions")
    public void filterByStatus_pending_returnsPendingTransactions() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Pending")
        .when()
                .get("/api/transactions/status")
        .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("Pending")));
    }

    @Test(priority = 12, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/status?status=Failure returns failed transactions")
    public void filterByStatus_failure_returnsFailedTransactions() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Failure")
        .when()
                .get("/api/transactions/status")
        .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("Failure")));
    }

    @Test(priority = 13, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/status with invalid status returns 400")
    public void filterByStatus_invalidValue_returns400() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Bogus")
        .when()
                .get("/api/transactions/status")
        .then()
                .statusCode(400);
    }

    // ── GET /api/transactions/user/{userId}/status ────────────────────────────

    @Test(priority = 14, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/user/{userId}/status filters user transactions by status")
    public void filterUserTransactionsByStatus_validParams_returnsList() {
        int uid = Integer.parseInt(TestContext.registeredUserId);
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Success")
        .when()
                .get("/api/transactions/user/" + uid + "/status")
        .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("Success")))
                .body("user.userId", everyItem(equalTo(uid)));
    }

    // ── GET /api/transactions/method?method=X ─────────────────────────────────

    @Test(priority = 15, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/method?method=UPI returns UPI transactions")
    public void filterByMethod_upi_returnsUpiTransactions() {
        given().spec(authSpec(TestContext.authToken)).queryParam("method", "UPI")
        .when()
                .get("/api/transactions/method")
        .then()
                .statusCode(200)
                .body("paymentMethod", everyItem(equalTo("UPI")));
    }

    @Test(priority = 16, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/method?method=Card returns Card transactions")
    public void filterByMethod_card_returnsCardTransactions() {
        given().spec(authSpec(TestContext.authToken)).queryParam("method", "Card")
        .when()
                .get("/api/transactions/method")
        .then()
                .statusCode(200)
                .body("paymentMethod", everyItem(equalTo("Card")));
    }

    @Test(priority = 17, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/method with invalid method returns 400")
    public void filterByMethod_invalidMethod_returns400() {
        given().spec(authSpec(TestContext.authToken)).queryParam("method", "Bitcoin")
        .when()
                .get("/api/transactions/method")
        .then()
                .statusCode(400);
    }

    // ── GET /api/transactions/user/{userId}/total ─────────────────────────────

    @Test(priority = 18, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/user/{userId}/total returns sum of successful payments")
    public void getTotalByUser_validUser_returnsSuccessfulTotal() {
        Response r = given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/user/" + TestContext.registeredUserId + "/total")
        .then()
                .statusCode(200)
                .extract().response();

        double total = parseBareNumber(r);
        org.testng.Assert.assertTrue(total >= 0.0, "Total should be non-negative, got " + total);
    }

    @Test(priority = 19, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "GET /api/transactions/user/{userId}/total for user with no transactions returns 0")
    public void getTotalByUser_noTransactions_returnsZero() {
        Response r = given().spec(authSpec(TestContext.authToken))
        .when()
                .get("/api/transactions/user/99999999/total")
        .then()
                .statusCode(200)
                .extract().response();

        org.testng.Assert.assertEquals(parseBareNumber(r), 0.0, 0.0001,
                "Total for user with no transactions should be 0");
    }

    // ── PATCH /api/transactions/{id}/status ───────────────────────────────────

    @Test(priority = 20, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "PATCH /api/transactions/{id}/status updates status and returns 200")
    public void updateTransactionStatus_validStatus_returns200() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Success")
        .when()
                .patch("/api/transactions/" + TestContext.transactionId + "/status")
        .then()
                .statusCode(200)
                .body("status", equalTo("Success"));
    }

    @Test(priority = 21, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "PATCH /api/transactions/{id}/status with invalid value returns 400")
    public void updateTransactionStatus_invalidValue_returns400() {
        given().spec(authSpec(TestContext.authToken)).queryParam("status", "Bogus")
        .when()
                .patch("/api/transactions/" + TestContext.transactionId + "/status")
        .then()
                .statusCode(400);
    }

    // ── DELETE /api/transactions/{id} ─────────────────────────────────────────

    @Test(priority = 22, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "DELETE /api/transactions/{id} returns 200 or 204")
    public void deleteTransaction_existingId_returns200Or204() {
        String id = createOwnTransaction(50.0, "Card");

        given().spec(authSpec(TestContext.authToken))
        .when()
                .delete("/api/transactions/" + id)
        .then()
                .statusCode(is(oneOf(200, 204)));
    }

    @Test(priority = 23, groups = {"transactions", "regression"}, retryAnalyzer = RetryAnalyzer.class,
          description = "DELETE /api/transactions/{id} for non-existent id returns 404")
    public void deleteTransaction_nonExistentId_returns404() {
        given().spec(authSpec(TestContext.authToken))
        .when()
                .delete("/api/transactions/99999999")
        .then()
                .statusCode(404);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String createOwnTransaction(double amount, String method) {
        return given().spec(authSpec(TestContext.authToken))
                .body(PayloadBuilder.createTransactionPayload(
                        TestContext.registeredUserId, TestContext.sessionId, amount, method))
        .when()
                .post("/api/transactions")
        .then()
                .statusCode(201)
                .extract().jsonPath().getString("transactionId");
    }

    /** The /total endpoint returns a bare JSON number, not an object — parse the raw body. */
    private static double parseBareNumber(Response r) {
        String body = r.asString().trim();
        try { return Double.parseDouble(body); } catch (NumberFormatException ignored) { }
        throw new AssertionError("Expected a bare numeric body, got: " + body);
    }
}
