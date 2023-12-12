package zarg.debitcredit.controllers;

import java.math.BigDecimal;

public record DebitAccountRequest(BigDecimal amount) {
}
