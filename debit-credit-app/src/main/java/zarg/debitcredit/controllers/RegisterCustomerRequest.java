package zarg.debitcredit.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterCustomerRequest {
    private String givenName;
    private String surname;
    private String emailAddress;
    private String password;
    private BigDecimal initialBalance;
}
