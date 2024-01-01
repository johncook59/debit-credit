package zarg.debitcredit.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.List;

@Entity
@Table(name = "customer",
        uniqueConstraints = @UniqueConstraint(columnNames = "email_address", name = "uk_email_address"),
        indexes = {
                @Index(name = "idx_customer_bid", columnList = "bid"),
                @Index(name = "idx_customer_email_address", columnList = "email_address")
        })
public class Customer {

    @Id
    @Column(name = "id", columnDefinition = "serial", nullable = false, updatable = false)
    @SequenceGenerator(name = "customer_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "bid", length = 12, updatable = false, insertable = false)
    @SequenceGenerator(name = "customer_bid_seq", allocationSize = 1)
    @ColumnDefault("concat('C', lpad(nextval('customer_bid_seq'::regclass)::text, 8, '0'))")
    @Generated(event = EventType.INSERT)
    private String bid;

    @Version
    private int version = 0;

    @Column(length = 20, nullable = false)
    private String password;

    @Column(name = "given_name", length = 40, nullable = false)
    private String givenName;

    @Column(length = 40, nullable = false)
    private String surname;

    @Column(name = "email_address", length = 60, nullable = false)
    private String emailAddress;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "customer_account",
            joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_customer_id_customer_account"),
            inverseJoinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
            inverseForeignKey = @ForeignKey(name = "fk_account_id_customer_account"))
    private List<Account> accounts;

    public Long getId() {
        return id;
    }

    public String getBid() {
        return bid;
    }

    public int getVersion() {
        return version;
    }

    public String getPassword() {
        return password;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public List<Account> getAccounts() {
        return accounts;
    }


    public static final class Builder {
        private Long id;
        private String bid;
        private int version;
        private String password;
        private String givenName;
        private String surname;
        private String emailAddress;
        private List<Account> accounts;

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

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder accounts(List<Account> accounts) {
            this.accounts = accounts;
            return this;
        }

        public Customer build() {
            Customer customer = new Customer();
            customer.givenName = this.givenName;
            customer.emailAddress = this.emailAddress;
            customer.bid = this.bid;
            customer.accounts = this.accounts;
            customer.surname = this.surname;
            customer.password = this.password;
            customer.id = this.id;
            customer.version = this.version;
            return customer;
        }
    }
}
