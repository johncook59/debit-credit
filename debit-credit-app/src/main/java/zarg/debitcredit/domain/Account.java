package zarg.debitcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "account", indexes = {@Index(name = "idx_account_bid", columnList = "bid")})
public class Account {

    @Id
    @Column(name = "id", columnDefinition = "serial", nullable = false, updatable = false)
    @SequenceGenerator(name = "account_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "bid", length = 12, updatable = false, insertable = false)
    @SequenceGenerator(name = "account_bid_seq", allocationSize = 1)
    @ColumnDefault("concat('A', lpad(nextval('account_bid_seq'::regclass)::text, 8, '0'))")
    @Generated(event = EventType.INSERT)
    private String bid;

    @Version
    private int version = 0;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(length = 20, nullable = false)
    private String name;

    public Long getId() {
        return id;
    }

    public String getBid() {
        return bid;
    }

    public int getVersion() {
        return version;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Account setBalance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public String getName() {
        return name;
    }

    public static final class Builder {
        private Long id;
        private String bid;
        private int version;
        private BigDecimal balance;
        private String name;

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

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Account build() {
            Account account = new Account();
            account.name = this.name;
            account.id = this.id;
            account.version = this.version;
            account.bid = this.bid;
            account.balance = this.balance;
            return account;
        }
    }
}
