import io.gatling.core.Predef._
import io.gatling.core.feeder.BatchableFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class DebitCreditSimulation extends Simulation {

  val lowContentionAccounts = "other_requests.csv"
  val highContentionAccounts = "top_requests.csv"

  val hcUsers = 5
  val users = 10
  val repeats = 20

  val creditFeederLow: BatchableFeederBuilder[String]#F = csv(lowContentionAccounts).shuffle.circular
  val debitFeederLow: BatchableFeederBuilder[String]#F = csv(lowContentionAccounts).shuffle.circular
  val balanceFeeder: BatchableFeederBuilder[String]#F = csv(lowContentionAccounts).shuffle.circular
  val highContentionFeeder: BatchableFeederBuilder[String]#F = csv(highContentionAccounts).shuffle.circular

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080/teller/")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling")

  val highContentionCreditDebit: ScenarioBuilder = scenario("Scenario High Contention")
    .feed(creditFeederLow)
    .repeat(repeats) {
      exec(
        http("High Contention Credit ")
          .put("${customer_bid}/credit")
          .body(StringBody("${credit_request}"))
          check status.is(200))
        .pause(1)
        .exec(http("High Contention Debit")
          .put("${customer_bid}/${account_bid}/debit")
          .body(StringBody("${debit_request}"))
          check status.is(200))
    }

  val lowContentionCredits: ScenarioBuilder = scenario("Scenario Credits")
    .feed(creditFeederLow)
    .repeat(repeats) {
      exec(
        http("Credit")
          .put("${customer_bid}/credit")
          .body(StringBody("${credit_request}"))
          check status.is(200))
    }

  val lowContentionDebits: ScenarioBuilder = scenario("Scenario Debits")
    .feed(debitFeederLow)
    .repeat(repeats) {
      exec(
        http("Debit")
          .put("${customer_bid}/${account_bid}/debit")
          .body(StringBody("${debit_request}"))
          check status.is(200))
    }

  val balance: ScenarioBuilder = scenario("Scenario Balances")
    .feed(balanceFeeder)
    .repeat(repeats) {
      exec(
        http("Balance")
          .get("${customer_bid}/${account_bid}/balance")
          check status.is(200))
    }

  setUp(
    lowContentionCredits
      .inject(atOnceUsers(users)),
    lowContentionDebits
      .inject(atOnceUsers(users)),
    balance
      .inject(atOnceUsers(users)),
    highContentionCreditDebit
      .inject(atOnceUsers(hcUsers)))
    .protocols(httpProtocol)
}
