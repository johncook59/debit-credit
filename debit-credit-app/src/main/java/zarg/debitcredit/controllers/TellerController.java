package zarg.debitcredit.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zarg.debitcredit.service.TellerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teller")
@Slf4j
class TellerController {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private final TellerService teller;

    public TellerController(TellerService teller) {
        this.teller = teller;
    }

    @GetMapping(value = "/{customerId}/{accountId}/balance")
    @ResponseBody
    public BalanceDetails balance(@PathVariable("customerId") String customerId, @PathVariable("accountId") String accountBid) {
        log.debug("Requesting balance for account {}", accountBid);
        return new BalanceDetails(accountBid, teller.balance(customerId, accountBid));
    }

    @PutMapping(value = "/{customerId}/credit")
    @ResponseBody
    public TransactionDetails credit(@PathVariable("customerId") String customerId, @RequestBody CreditAccountRequest request) {
        log.debug("Requesting {} credit to account {}", request.amount(), request.accountId());
        validateAmount(request.amount());

        return new TransactionDetails(teller.credit(customerId, request.accountId(), request.amount()));
    }

    @PutMapping(value = "/{customerId}/{accountId}/debit")
    @ResponseBody
    public TransactionDetails debit(@PathVariable("customerId") String customerId,
                                    @PathVariable("accountId") String accountId,
                                    @RequestBody DebitAccountRequest request) {
        log.debug("Requesting {} debit from account {}", request.amount(), accountId);
        validateAmount(request.amount());

        return new TransactionDetails(teller.debit(customerId, accountId, request.amount()));
    }

    @GetMapping(value = "/{customerId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<TransactionDetails>> transactions(@PathVariable("customerId") String customerId,
                                                              @RequestParam(name = "from", required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime from,
                                                              @RequestParam(name = "to", required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime to) {

        if (from == null) {
            from = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        }

        if (to == null) {
            to = LocalDateTime.now(ZoneOffset.UTC);
        }

        log.info("Finding transactions for {} from {}, to {}", customerId, from, to);

        return teller.findTransactions(customerId, from, to)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(TransactionDetails::new)
                                .toList()));
    }

    private void validateAmount(BigDecimal amount) {
        Assert.notNull(amount, "Null amount");
        Assert.isTrue(amount.doubleValue() > 0, "amount not greater than zero");
    }
}
