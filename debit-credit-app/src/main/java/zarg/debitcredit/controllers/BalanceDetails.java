package zarg.debitcredit.controllers;

import java.math.BigDecimal;

public record BalanceDetails(String accountId, BigDecimal balance) {
}
