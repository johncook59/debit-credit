package zarg.debitcredit.controllers;

import zarg.debitcredit.domain.Account;
import zarg.debitcredit.domain.Customer;

import java.util.List;

public record CustomerDetails(String bid,
                              String givenName,
                              String surname,
                              String emailAddress,
                              List<String> accounts) {

    public CustomerDetails(Customer customer) {
        this(customer.getBid(),
                customer.getGivenName(),
                customer.getSurname(),
                customer.getEmailAddress(),
                customer.getAccounts().stream()
                        .map(Account::getBid)
                        .toList());
    }
}
