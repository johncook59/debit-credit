package zarg.debitcredit.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zarg.debitcredit.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TellerService {
    Transaction credit(String customerId, String accountBid, BigDecimal amount);

    Transaction debit(String customerId, String accountBid, BigDecimal amount);

    BigDecimal balance(String customerId, String accountBid);

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    Map<String, List<Transaction>> findTransactions(String customerBid, LocalDateTime from, LocalDateTime to);
}
