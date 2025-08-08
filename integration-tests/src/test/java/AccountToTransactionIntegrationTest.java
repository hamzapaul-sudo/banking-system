import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountToTransactionIntegrationTest {

    private static Long accountId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @Order(1)
    void createAccount() {
        RestAssured.port = 8081; // account-service

        String body = """
            {
              "accountHolder": "Integration Test",
              "balance": 1000.0,
              "customerId": 500,
              "type": "CHECKING"
            }
        """;

        accountId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        System.out.println("✅ Created account with ID: " + accountId);
    }

    @Test
    @Order(2)
    void depositAndVerifyTransactionLogged() throws InterruptedException {
        RestAssured.port = 8081;

        String deposit = """
            {
              "accountId": %d,
              "amount": 300.0
            }
        """.formatted(accountId);

        given()
                .contentType(ContentType.JSON)
                .body(deposit)
                .post("/api/accounts/deposit")
                .then()
                .statusCode(200);

        // Wait for transaction-service to consume from Kafka and persist
        Thread.sleep(3000); // optional — tune based on system speed

        RestAssured.port = 8082; // transaction-service

        List<Map<String, Object>> txList = given()
                        .when()
                        .get("/api/transactions/account/" + accountId)
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("");

        assertThat("Transaction list should not be empty", txList, is(not(empty())));

        Map<String, Object> tx = txList.getFirst();

        assertThat(tx.get("accountId"), is(accountId.intValue()));
        assertThat(tx.get("type"), is("DEPOSIT"));
        assertThat(tx.get("amount"), is(300.0f));
        assertThat(tx.get("description"), is("Deposit to account"));

        System.out.println("✅ Transaction verified via transaction-service API");
    }
}
