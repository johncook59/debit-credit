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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;

@Entity
@Table(name = "account", indexes = {@Index(name = "idx_account_bid", columnList = "bid")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private int version = 0;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(length = 20, nullable = false)
    private String name;
}
