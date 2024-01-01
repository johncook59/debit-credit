package zarg.debitcredit.service;

import org.springframework.stereotype.Service;
import zarg.debitcredit.domain.Account;

@Service
class DefaultAccountAccessControlService implements AccountAccessControlService {

    private final CustomerService customerService;

    DefaultAccountAccessControlService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public boolean canDebit(String customerBid, Account account) {
        return isCustomerAccountOwner(customerBid, account);
    }

    @Override
    public boolean canReadBalance(String customerBid, Account account) {
        return isCustomerAccountOwner(customerBid, account);
    }

    private boolean isCustomerAccountOwner(String customerBid, Account account) {
        return customerService.isAccountOwner(customerBid, account.getBid());
    }
}
