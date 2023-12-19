package zarg.debitcredit.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import zarg.debitcredit.dao.AccountDao;
import zarg.debitcredit.dao.CustomerDao;
import zarg.debitcredit.dao.TransactionDao;
import zarg.debitcredit.domain.Account;
import zarg.debitcredit.domain.Customer;
import zarg.debitcredit.domain.Transaction;
import zarg.debitcredit.domain.TransactionType;
import zarg.debitcredit.events.CreditEvent;
import zarg.debitcredit.events.CreditFailedEvent;
import zarg.debitcredit.events.DebitEvent;
import zarg.debitcredit.events.DebitFailedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static zarg.debitcredit.domain.TransactionType.CREDIT;
import static zarg.debitcredit.domain.TransactionType.DEBIT;

@Service
class DefaultTellerService implements TellerService {

    private static final String FAILED_TO_FIND_ACCOUNT = "Failed to find account %s";
    private static final String FAILED_TO_FIND_CUSTOMER = "Failed to find customer %s";
    private static final String FAILED_TO_FIND_OR_LOCK_ACCOUNT = "Failed to find or lock account %s";
    private static final String ACCOUNT_FOR_CUSTOMER_NOT_FOUND = "Account %s for customer %s not found";
    private final AccountAccessControlService accountAccessControlService;
    private final CustomerDao customerDao;
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final ApplicationEventPublisher publisher;

    DefaultTellerService(AccountAccessControlService accountAccessControlService,
                         CustomerDao customerDao,
                         AccountDao accountDao,
                         TransactionDao transactionDao,
                         ApplicationEventPublisher publisher) {
        this.accountAccessControlService = accountAccessControlService;
        this.customerDao = customerDao;
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public Transaction credit(String customerBid, String accountBid, BigDecimal amount) {
        try {
            Account account = accountDao.findAndLockByBid(accountBid)
                    .orElseThrow(() -> new EntityNotFoundException(
                            FAILED_TO_FIND_OR_LOCK_ACCOUNT.formatted(accountBid)));

            if (!accountAccessControlService.canCredit(customerBid, account)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountBid, customerBid));
            }

            Transaction transaction = updateBalance(customerBid, account, amount, CREDIT);
            publisher.publishEvent(new CreditEvent(customerBid, accountBid, amount));

            return transaction;
        } catch (Exception e) {
            publisher.publishEvent(new CreditFailedEvent(customerBid, accountBid, e.getMessage(), amount));
            throw e;
        }
    }

    @Transactional
    @Override
    public Transaction debit(String customerBid, String accountBid, BigDecimal amount) {
        try {
            Account account = accountDao.findAndLockByBid(accountBid)
                    .orElseThrow(() -> new EntityNotFoundException(
                            FAILED_TO_FIND_OR_LOCK_ACCOUNT.formatted(accountBid)));

            if (!accountAccessControlService.canDebit(customerBid, account)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountBid, customerBid));
            }

            Transaction transaction = updateBalance(customerBid, account, amount, DEBIT);
            publisher.publishEvent(new DebitEvent(customerBid, accountBid, amount));

            return transaction;
        } catch (Exception e) {
            publisher.publishEvent(new DebitFailedEvent(customerBid, accountBid, e.getMessage(), amount));
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal balance(String customerBid, String accountBid) {
        Account account = accountDao.findByBid(accountBid)
                .orElseThrow(() -> new EntityNotFoundException(
                        FAILED_TO_FIND_ACCOUNT.formatted(accountBid)));

        if (!accountAccessControlService.canReadBalance(customerBid, account)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountBid, customerBid));
        }

        return account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Transaction>> findTransactions(String customerBid, LocalDateTime from, LocalDateTime to) {
        Customer customer = customerDao.findByBid(customerBid)
                .orElseThrow(() -> new EntityNotFoundException(FAILED_TO_FIND_CUSTOMER.formatted(customerBid)));

        return customer.getAccounts().stream()
                .collect(Collectors.toMap(Account::getBid,
                        account -> transactionDao.findByAccountBidAndProcessedBetween(account.getBid(), from, to)));
    }

    private Transaction updateBalance(String customerBid, Account account, BigDecimal amount,
                                      TransactionType transactionType) {
        account.setBalance(account.getBalance().add(transactionType == CREDIT ? amount : amount.negate()));
        accountDao.save(account);

        Transaction transaction = Transaction.Builder.builder()
                .accountBid(account.getBid())
                .userBid(customerBid)
                .amount(amount)
                .balance(account.getBalance())
                .type(transactionType)
                .processed(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return transactionDao.save(transaction);
    }
}
