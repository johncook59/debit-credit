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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
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
}
