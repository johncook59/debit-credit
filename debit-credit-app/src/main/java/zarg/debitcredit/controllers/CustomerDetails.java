package zarg.debitcredit.controllers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import zarg.debitcredit.domain.Account;
import zarg.debitcredit.domain.Customer;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class CustomerDetails {

    public CustomerDetails(Customer customer) {
        this.bid = customer.getBid();
        this.givenName = customer.getGivenName();
        this.surname = customer.getSurname();
        this.emailAddress = customer.getEmailAddress();
        this.accounts = customer.getAccounts().stream()
            .map(Account::getBid)
            .collect(Collectors.toList());
    }

    private String bid;
    private String givenName;
    private String surname;
    private String emailAddress;
    private List<String> accounts;
}
