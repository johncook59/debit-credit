package zarg.debitcredit.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zarg.debitcredit.domain.Customer;

import java.util.Optional;

@Repository
public interface CustomerDao extends JpaRepository<Customer, Long> {
    Optional<Customer> findByBid(String bid);

    @Query(value = "select c.* " +
            "from customer c " +
            "join customer_account ca on ca.customer_id = c.id " +
            "join account a on ca.account_id = a.id " +
            "where c.bid = ?1 " +
            "and a.bid = ?2",
            nativeQuery = true)
    Optional<Customer> findByBidAndAccountBid(String customerBid, String accountBid);
}
