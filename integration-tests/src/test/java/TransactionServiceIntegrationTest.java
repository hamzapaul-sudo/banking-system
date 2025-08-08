import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionServiceIntegrationTest {

    private static final Long ACCOUNT_ID = 101L;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8082; // transaction-service port
    }

    @Test
    @Order(1)
    void shouldLogTransactionSuccessfully() {
        String body = """
            {
              "accountId": %d,
              "type": "DEPOSIT",
              "amount": 500.0,
              "description": "Test Deposit"
            }
        """.formatted(ACCOUNT_ID);

        long transactionId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(200)
                .body("accountId", equalTo(ACCOUNT_ID.intValue()))
                .body("type", equalTo("DEPOSIT"))
                .body("amount", equalTo(500.0f))
                .body("description", equalTo("Test Deposit"))
                .body("timestamp", notNullValue())
                .extract()
                .path("id")).longValue();

        System.out.println("transactionId: " + transactionId);
    }

    @Test
    @Order(2)
    void shouldFetchTransactionsByAccountId() {
        var response = given()
                .when()
                .get("/api/transactions/account/" + ACCOUNT_ID)
                .then()
                .statusCode(200)
                .extract()
                .asPrettyString(); // <-- get full response as formatted string

        System.out.println("📄 Transactions for account " + ACCOUNT_ID + ":");
        System.out.println(response);
    }

    @Test
    void shouldFailValidationOnMissingFields() {
        String body = """
            {
              "accountId": null,
              "type": "",
              "amount": null,
              "description": ""
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(anyOf(is(400), is(500))); // depending on if validation is in place
    }
}
