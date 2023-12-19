package zarg.debitcredit.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class TellerController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private final TellerService teller;

    public TellerController(TellerService teller) {
        this.teller = teller;
    }

    @GetMapping(value = "/{customerId}/{accountId}")
    @ResponseBody
    public BalanceDetails balance(@PathVariable("customerId") String customerBid,
                                  @PathVariable("accountId") String accountBid) {
        log.debug("Requesting balance by {} for account {}", customerBid, accountBid);
        return new BalanceDetails(accountBid, teller.balance(customerBid, accountBid));
    }

    @PutMapping(value = "/{customerId}/{accountId}")
    @ResponseBody
    public TransactionDetails tellerTransaction(@PathVariable("customerId") String customerBid,
                                                @PathVariable("accountId") String accountBid,
                                                @RequestBody TellerRequest request) {
        log.debug("Requesting {} {} by {} from account {}", request.type(),
                request.amount(), customerBid, accountBid);
        validateAmount(request.amount());

        return new TransactionDetails(switch (request.type()) {
            case DEBIT -> teller.debit(customerBid, accountBid, request.amount());
            case CREDIT -> teller.credit(customerBid, accountBid, request.amount());
        });
    }

    @GetMapping(value = "/{customerId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<TransactionDetails>> transactionHistory(
            @PathVariable("customerId") String customerBid,
            @RequestParam(name = "from", required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime to) {

        if (from == null) {
            from = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        }

        if (to == null) {
            to = LocalDateTime.now(ZoneOffset.UTC);
        }

        log.info("Finding transactions for {} from {}, to {}", customerBid, from, to);

        return teller.findTransactions(customerBid, from, to)
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
