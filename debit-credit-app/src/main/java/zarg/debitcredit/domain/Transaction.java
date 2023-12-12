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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_account_bid", columnList = "account_bid")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
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
}
