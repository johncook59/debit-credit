package zarg.debitcredit.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditAccountRequest {
    private String accountId;
    private BigDecimal amount;
}
