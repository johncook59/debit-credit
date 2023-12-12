package zarg.debitcredit;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class DebitCreditSimulation extends Simulation {

    private static final String LOW_CONTENTION_ACCOUNTS = "other_requests.csv";
    private static final String HIGH_CONTENTION_ACCOUNTS = "top_requests.csv";

    private static final int HC_USERS = 5;
    private static final int NORMAL_USERS = 20;
    private static final int DURATION_SECS = 500;
    private static final Duration DURATION = Duration.ofSeconds(DURATION_SECS);

    public DebitCreditSimulation() {

        FeederBuilder.Batchable<String> creditFeederLow = csv(LOW_CONTENTION_ACCOUNTS)
                .shuffle()
                .circular();
        FeederBuilder.Batchable<String> debitFeederLow = csv(LOW_CONTENTION_ACCOUNTS)
                .shuffle()
                .circular();
        FeederBuilder.Batchable<String> balanceFeeder = csv(LOW_CONTENTION_ACCOUNTS)
                .shuffle()
                .circular();
        FeederBuilder.Batchable<String> highContentionFeeder = csv(HIGH_CONTENTION_ACCOUNTS)
                .shuffle()
                .circular();

        HttpProtocolBuilder httpProtocol = http
                .baseUrl("http://localhost:8000/teller/")
                .contentTypeHeader(MediaType.APPLICATION_JSON_VALUE)
                .acceptHeader(MediaType.APPLICATION_JSON_VALUE)
                .userAgentHeader("Gatling");

        ScenarioBuilder highContentionCreditDebit = scenario("Scenario High Contention")
                .feed(highContentionFeeder)
                .during(DURATION)
                .on(
                        exec(http("High Contention Credit ")
                                .put("#{customer_bid}/credit")
                                .body(StringBody("#{credit_request}"))
                                .check(status()
                                        .is(200)))
                                .pause(1)
                                .exec(http("High Contention Debit")
                                        .put("#{customer_bid}/#{account_bid}/debit")
                                        .body(StringBody("#{debit_request}"))
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder lowContentionCredits = scenario("Scenario Credits")
                .feed(creditFeederLow)
                .during(DURATION)
                .on(
                        exec(
                                http("Credit")
                                        .put("#{customer_bid}/credit")
                                        .body(StringBody("#{credit_request}"))
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder lowContentionDebits = scenario("Scenario Debits")
                .feed(debitFeederLow)
                .during(DURATION)
                .on(
                        exec(
                                http("Debit")
                                        .put("#{customer_bid}/#{account_bid}/debit")
                                        .body(StringBody("#{debit_request}"))
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder balance = scenario("Scenario Balances")
                .feed(balanceFeeder)
                .during(DURATION)
                .on(
                        exec(
                                http("Balance")
                                        .get("#{customer_bid}/#{account_bid}/balance")
                                        .check(status()
                                                .is(200))));

        setUp(lowContentionCredits
                        .injectClosed(getConstantConcurrentUsers(NORMAL_USERS)),
                lowContentionDebits
                        .injectClosed(getConstantConcurrentUsers(NORMAL_USERS)),
                balance
                        .injectClosed(getConstantConcurrentUsers(NORMAL_USERS)),
                highContentionCreditDebit
                        .injectClosed(getConstantConcurrentUsers(HC_USERS))
        ).protocols(httpProtocol);
    }

    @NotNull
    private static ClosedInjectionStep getConstantConcurrentUsers(int users) {
        return constantConcurrentUsers(users)
                .during(DURATION);
    }
}
