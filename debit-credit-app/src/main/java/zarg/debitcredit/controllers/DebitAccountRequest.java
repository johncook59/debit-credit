package zarg.debitcredit.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitAccountRequest {
    private BigDecimal amount;
}
