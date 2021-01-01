package zarg.debitcredit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;
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
    @GeneratedValue
    private Long id;

    @Column(name = "bid", length = 12, updatable = false, insertable = false)
    @ColumnDefault("concat('T', lpad(nextval('hibernate_sequence'::regclass)::text, 10, '0'))")
    @Generated(GenerationTime.INSERT)
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
