package zarg.debitcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_account_bid", columnList = "account_bid")
})
public class Transaction {

    @Id
    @Column(name = "id", columnDefinition = "serial", nullable = false, updatable = false)
    @SequenceGenerator(name = "transaction_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "bid", length = 12, updatable = false, insertable = false)
    @SequenceGenerator(name = "transaction_bid_seq", allocationSize = 1)
    @ColumnDefault("concat('T', lpad(nextval('transaction_bid_seq'::regclass)::text, 10, '0'))")
    @Generated(event = EventType.INSERT)
    private String bid;

    @Column(name = "user_bid", length = 12, nullable = false)
    private String userBid;

    @Version
    private int version = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    public TransactionDirection direction;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "account_bid", nullable = false)
    private String accountBid;

    @Column(name = "processed_dt", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime processed;

    public Long getId() {
        return id;
    }

    public String getBid() {
        return bid;
    }

    public String getUserBid() {
        return userBid;
    }

    public int getVersion() {
        return version;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getAccountBid() {
        return accountBid;
    }

    public LocalDateTime getProcessed() {
        return processed;
    }


    public static final class Builder {
        private Long id;
        private String bid;
        private String userBid;
        private int version;
        private TransactionDirection direction;
        private BigDecimal amount;
        private BigDecimal balance;
        private String accountBid;
        private LocalDateTime processed;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder bid(String bid) {
            this.bid = bid;
            return this;
        }

        public Builder userBid(String userBid) {
            this.userBid = userBid;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder direction(TransactionDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder accountBid(String accountBid) {
            this.accountBid = accountBid;
            return this;
        }

        public Builder processed(LocalDateTime processed) {
            this.processed = processed;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.amount = this.amount;
            transaction.id = this.id;
            transaction.userBid = this.userBid;
            transaction.processed = this.processed;
            transaction.accountBid = this.accountBid;
            transaction.version = this.version;
            transaction.direction = this.direction;
            transaction.bid = this.bid;
            transaction.balance = this.balance;
            return transaction;
        }
    }
}
