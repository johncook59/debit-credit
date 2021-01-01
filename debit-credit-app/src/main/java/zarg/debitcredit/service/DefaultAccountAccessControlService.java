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
    public boolean canDebit(String customerId, Account account) {
        return isCustomerAccountOwner(customerId, account);
    }

    @Override
    public boolean canReadBalance(String customerId, Account account) {
        return isCustomerAccountOwner(customerId, account);
    }

    private boolean isCustomerAccountOwner(String customerId, Account account) {
        return customerService.isAccountOwner(customerId, account.getBid());
    }
}
