package zarg.debitcredit;

import com.intuit.karate.junit5.Karate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import zarg.debitcredit.controllers.RegisterCustomerRequest;
import zarg.debitcredit.domain.Customer;
import zarg.debitcredit.service.CustomerService;

import java.math.BigDecimal;

@SpringBootTest(
        classes = {DebitCreditApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EndToEndKarateIT {

    @LocalServerPort
    private String localServerPort;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine")
            .withDatabaseName("debit_credit")
            .withInitScript("prereq-sequences.sql");

    @Autowired
    private CustomerService customerService;

    private String customer1Bid;
    private String account1Bid;
    private String customer2Bid;
    private String account2Bid;
    private String customer3Bid;
    private String account3Bid;

    @BeforeEach
    void setupDB() {
        Customer customer1 = createCustomer("One");
        customer1Bid = customer1.getBid();
        account1Bid = customer1.getAccounts().getFirst().getBid();

        Customer customer2 = createCustomer("Two");
        customer2Bid = customer2.getBid();
        account2Bid = customer2.getAccounts().getFirst().getBid();

        Customer customer3 = createCustomer("Three");
        customer3Bid = customer3.getBid();
        account3Bid = customer3.getAccounts().getFirst().getBid();
    }

    @Karate.Test
    Karate testTellerAPI() {
        return Karate
                .run("teller")
                .systemProperty("local_port", localServerPort)
                .systemProperty("customer_1_bid", customer1Bid)
                .systemProperty("account_1_bid", account1Bid)
                .systemProperty("customer_2_bid", customer2Bid)
                .systemProperty("account_2_bid", account2Bid)
                .systemProperty("customer_3_bid", customer3Bid)
                .systemProperty("account_3_bid", account3Bid)
                .relativeTo(getClass());
    }

    @NotNull
    private Customer createCustomer(String suffix) {
        return customerService.registerCustomer(
                RegisterCustomerRequest.Builder.builder()
                        .givenName("test%s".formatted(suffix))
                        .surname("me")
                        .emailAddress("test%s.me@test.com".formatted(suffix))
                        .initialBalance(BigDecimal.TEN)
                        .password("letmein")
                        .build());
    }
}