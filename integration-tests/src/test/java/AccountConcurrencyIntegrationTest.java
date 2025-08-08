import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;

public class AccountConcurrencyIntegrationTest {

    private static Long accountId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081;

        // Create an account with 1000 balance
        String body = """
            {
              "accountHolder": "Concurrent User",
              "balance": 1000.0,
              "customerId": 88,
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
    }

    @Test
    @Order(1)
    void concurrentWithdrawals_shouldOnlyAllowOne() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable withdrawTask = () -> {
            try {
                latch.await(); // Wait until both threads are ready

                String withdrawBody = """
                    {
                      "accountId": %d,
                      "amount": 800.0
                    }
                """.formatted(accountId);

                var response = given()
                        .contentType(ContentType.JSON)
                        .body(withdrawBody)
                        .post("/api/accounts/withdraw");

                if (response.statusCode() == 200) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executor.submit(withdrawTask);
        executor.submit(withdrawTask);

        latch.countDown(); // Start both threads
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // One should succeed, one should fail
        Assertions.assertEquals(1, successCount.get(), "Only one withdraw should succeed");
        Assertions.assertEquals(1, failureCount.get(), "Other withdraw should fail");

        // ✅ Print the final balance
        double finalBalance = ((Number) given()
                .get("/api/accounts/" + accountId)
                .then()
                .statusCode(200)
                .extract()
                .path("balance")).doubleValue();

        System.out.printf("🧾 Final balance after concurrent withdrawals: %.2f%n", finalBalance);
    }

    @Test
    @Order(2)
    void concurrentDeposits_shouldBothSucceedAndUpdateBalance() throws InterruptedException {
        // Create a new account with 1000 balance
        String body = """
        {
          "accountHolder": "Deposit User",
          "balance": 1000.0,
          "customerId": 99,
          "type": "SAVINGS"
        }
    """;

        Long depositAccountId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(body)
                .post("/api/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable depositTask = () -> {
            try {
                latch.await();

                String depositBody = """
                {
                  "accountId": %d,
                  "amount": 500.0
                }
            """.formatted(depositAccountId);

                given()
                        .contentType(ContentType.JSON)
                        .body(depositBody)
                        .post("/api/accounts/deposit")
                        .then()
                        .statusCode(200);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        executor.submit(depositTask);
        executor.submit(depositTask);

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        double finalBalance = ((Number) given()
                .get("/api/accounts/" + depositAccountId)
                .then()
                .statusCode(200)
                .extract()
                .path("balance")).doubleValue();

        System.out.printf("💰 Final balance after concurrent deposits: %.2f%n", finalBalance);

        Assertions.assertEquals(2000.0, finalBalance, 0.001, "Both deposits should be applied");
    }

}
