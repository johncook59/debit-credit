package zarg.debitcredit;

import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

public class DebitCreditSimulation extends Simulation {

    private static final String LOW_CONTENTION_ACCOUNTS = "other_requests.csv";
    private static final String HIGH_CONTENTION_ACCOUNTS = "top_requests.csv";

    private static final int HC_USERS = 10;
    private static final int NORMAL_USERS = 30;
    private static final int DURATION_SECS = 300;
    private static final Duration DURATION = Duration.ofSeconds(DURATION_SECS);
    private static final Body CREDIT_REQUEST = StringBody(
            """
            {
              "type": "CREDIT",
              "amount": 1.00
            }
            """);
    private static final Body DEBIT_REQUEST = StringBody(
             """
             {
               "type": "DEBIT",
               "amount": 1.00
             }
             """);
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
                .header(CONTENT_TYPE, APPLICATION_JSON.toString())
                .header(ACCEPT, APPLICATION_JSON.toString())
                .userAgentHeader("Gatling");

        ScenarioBuilder highContentionCreditDebit = scenario("Scenario High Contention")
                .feed(highContentionFeeder)
                .during(DURATION)
                .on(
                        exec(http("High Contention Credit")
                                .put("#{customer_bid}/#{account_bid}")
                                .body(CREDIT_REQUEST)
                                .check(status()
                                        .is(200)))
                                .pause(1)
                                .exec(http("High Contention Debit")
                                        .put("#{customer_bid}/#{account_bid}")
                                        .body(DEBIT_REQUEST)
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder lowContentionCredits = scenario("Scenario Credits")
                .feed(creditFeederLow)
                .during(DURATION)
                .on(
                        exec(
                                http("Credit")
                                        .put("#{customer_bid}/#{account_bid}")
                                        .body(CREDIT_REQUEST)
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder lowContentionDebits = scenario("Scenario Debits")
                .feed(debitFeederLow)
                .during(DURATION)
                .on(
                        exec(
                                http("Debit")
                                        .put("#{customer_bid}/#{account_bid}")
                                        .body(DEBIT_REQUEST)
                                        .check(status()
                                                .is(200))));

        ScenarioBuilder balance = scenario("Scenario Balances")
                .feed(balanceFeeder)
                .during(DURATION)
                .on(
                        exec(
                                http("Balance")
                                        .get("#{customer_bid}/#{account_bid}")
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

    private static ClosedInjectionStep getConstantConcurrentUsers(int users) {
        return constantConcurrentUsers(users)
                .during(DURATION);
    }
}
