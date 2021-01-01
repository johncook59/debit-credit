package zarg.debitcredit.controllers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import zarg.debitcredit.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TransactionDetails {

    public TransactionDetails(Transaction transaction) {
        id = transaction.getBid();
        direction = transaction.getDirection().name();
        amount = transaction.getAmount();
        userId = transaction.getUserBid();
        balance = transaction.getBalance();
        accountId = transaction.getAccountBid();
        processed = transaction.getProcessed();
    }

    private String id;

    private String direction;

    private BigDecimal amount;

    private BigDecimal balance;

    private String userId;

    private String accountId;

    private LocalDateTime processed;
}
