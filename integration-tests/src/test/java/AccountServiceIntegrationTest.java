import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceIntegrationTest {

    private static Long accountId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081;
    }

    @Test
    @Order(1)
    void createAccount_withCustomerAndType_shouldSucceed() {
        String body = """
            {
              "accountHolder": "Hamza",
              "balance": 1000.0,
              "customerId": 99,
              "type": "SAVINGS"
            }
        """;

        accountId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/accounts")
                .then()
                .statusCode(200)
                .body("accountHolder", equalTo("Hamza"))
                .body("customerId", equalTo(99))
                .body("type", equalTo("SAVINGS"))
                .body("status", equalTo("ACTIVE"))
                .body("createdAt", notNullValue())
                .extract()
                .path("id")).longValue();
    }

    @Test
    @Order(2)
    void depositOnActiveAccount_shouldSucceed() {
        String body = """
            {
              "accountId": %d,
              "amount": 300.0
            }
        """.formatted(accountId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/accounts/deposit")
                .then()
                .statusCode(200)
                .body("balance", equalTo(1300.0f));
    }

    @Test
    @Order(3)
    void withdrawOnActiveAccount_shouldSucceed() {
        String body = """
            {
              "accountId": %d,
              "amount": 300.0
            }
        """.formatted(accountId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/accounts/withdraw")
                .then()
                .statusCode(200)
                .body("balance", equalTo(1000.0f));
    }

    @Test
    @Order(4)
    void softDeleteAccount_shouldMarkClosedAndHidden() {
        given()
                .delete("/api/accounts/" + accountId)
                .then()
                .statusCode(200);

        // Confirm not visible anymore
        given()
                .get("/api/accounts/" + accountId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void depositOnClosedAccount_shouldFail() {
        String body = """
            {
              "accountId": %d,
              "amount": 100.0
            }
        """.formatted(accountId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/accounts/deposit")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void paginationShouldReturnValidPage() {
        // Create 2 accounts
        for (int i = 0; i < 2; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                        {
                          "accountHolder": "User%d",
                          "balance": 100.0,
                          "customerId": %d,
                          "type": "CHECKING"
                        }
                    """.formatted(i, 100 + i))
                    .post("/api/accounts")
                    .then()
                    .statusCode(200);
        }

        // Get page size = 1
        given()
                .get("/api/accounts?page=0&size=1")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1));
    }

    @Test
    void missingFields_shouldFailValidation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "accountHolder": "",
                      "balance": -100,
                      "customerId": null,
                      "type": null
                    }
                """)
                .post("/api/accounts")
                .then()
                .statusCode(400)
                .body("message.accountHolder", notNullValue())
                .body("message.balance", notNullValue())
                .body("message.customerId", notNullValue())
                .body("message.type", notNullValue());
    }
}
