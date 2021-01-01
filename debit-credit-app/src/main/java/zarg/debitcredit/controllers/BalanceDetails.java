package zarg.debitcredit.controllers;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class BalanceDetails {

    public BalanceDetails(String accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    private String accountId;
    private BigDecimal balance;
}
