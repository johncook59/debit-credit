package zarg.debitcredit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
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
    @GeneratedValue
    private Long id;

    @Column(name = "bid", length = 12, updatable = false, insertable = false)
    @ColumnDefault("concat('C', lpad(nextval('hibernate_sequence'::regclass)::text, 8, '0'))")
    @Generated(GenerationTime.INSERT)
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
