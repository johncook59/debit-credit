package zarg.debitcredit.controllers;

import zarg.debitcredit.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDetails(
        String id,
        String direction,
        BigDecimal amount,
        BigDecimal balance,
        String userId,
        String accountId,
        LocalDateTime processed) {

    public TransactionDetails(Transaction transaction) {
        this(
                transaction.getBid(),
                transaction.getDirection().name(),
                transaction.getAmount(),
                transaction.getBalance(),
                transaction.getUserBid(),
                transaction.getAccountBid(),
                transaction.getProcessed());
    }
}
