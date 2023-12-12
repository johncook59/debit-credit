package zarg.debitcredit.controllers;

import java.math.BigDecimal;

public record CreditAccountRequest(String accountId, BigDecimal amount) {
}
