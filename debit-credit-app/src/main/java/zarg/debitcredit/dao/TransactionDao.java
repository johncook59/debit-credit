package zarg.debitcredit.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zarg.debitcredit.domain.Transaction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountBidAndProcessedBetween(String accountBid, LocalDateTime from, LocalDateTime to);
}
