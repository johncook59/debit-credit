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
import zarg.debitcredit.domain.TransactionDirection;
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

import static zarg.debitcredit.domain.TransactionDirection.CREDIT;
import static zarg.debitcredit.domain.TransactionDirection.DEBIT;

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
    public Transaction credit(String customerId, String accountId, BigDecimal amount) {
        try {
            Account account = accountDao.findAndLockByBid(accountId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            FAILED_TO_FIND_OR_LOCK_ACCOUNT.formatted(accountId)));

            if (!accountAccessControlService.canCredit(customerId, account)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountId, customerId));
            }

            Transaction transaction = updateBalance(customerId, account, amount, CREDIT);
            publisher.publishEvent(new CreditEvent(customerId, accountId, amount));

            return transaction;
        } catch (Exception e) {
            publisher.publishEvent(new CreditFailedEvent(customerId, accountId, e.getMessage(), amount));
            throw e;
        }
    }

    @Transactional
    @Override
    public Transaction debit(String customerId, String accountId, BigDecimal amount) {
        try {
            Account account = accountDao.findAndLockByBid(accountId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            FAILED_TO_FIND_OR_LOCK_ACCOUNT.formatted(accountId)));

            if (!accountAccessControlService.canDebit(customerId, account)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountId, customerId));
            }

            Transaction transaction = updateBalance(customerId, account, amount, DEBIT);
            publisher.publishEvent(new DebitEvent(customerId, accountId, amount));

            return transaction;
        } catch (Exception e) {
            publisher.publishEvent(new DebitFailedEvent(customerId, accountId, e.getMessage(), amount));
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal balance(String customerId, String accountId) {
        Account account = accountDao.findByBid(accountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        FAILED_TO_FIND_ACCOUNT.formatted(accountId)));

        if (!accountAccessControlService.canReadBalance(customerId, account)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    ACCOUNT_FOR_CUSTOMER_NOT_FOUND.formatted(accountId, customerId));
        }

        return account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Transaction>> findTransactions(String customerId, LocalDateTime from, LocalDateTime to) {
        Customer customer = customerDao.findByBid(customerId)
                .orElseThrow(() -> new EntityNotFoundException(FAILED_TO_FIND_CUSTOMER.formatted(customerId)));

        return customer.getAccounts().stream()
                .collect(Collectors.toMap(Account::getBid,
                        account -> transactionDao.findByAccountBidAndProcessedBetween(account.getBid(), from, to)));
    }

    private Transaction updateBalance(String customerId, Account account, BigDecimal amount, TransactionDirection direction) {
        account.setBalance(account.getBalance().add(direction == CREDIT ? amount : amount.negate()));
        accountDao.save(account);

        Transaction transaction = Transaction.Builder.builder()
                .accountBid(account.getBid())
                .userBid(customerId)
                .amount(amount)
                .balance(account.getBalance())
                .direction(direction)
                .processed(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return transactionDao.save(transaction);
    }
}
