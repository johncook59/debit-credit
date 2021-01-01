package zarg.debitcredit.service;

import zarg.debitcredit.domain.Account;

public interface AccountAccessControlService {
    default boolean canCredit(String customerId, Account account) {
        return true;
    }

    boolean canDebit(String customerId, Account account);

    boolean canReadBalance(String customerId, Account account);
}
