package zarg.debitcredit.controllers;

import zarg.debitcredit.domain.TransactionType;

import java.math.BigDecimal;

public record TellerRequest(BigDecimal amount, TransactionType type) {
}
