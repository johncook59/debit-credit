package zarg.debitcredit.events;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString(callSuper = true)
public class DebitFailedEvent extends TellerEvent {
    private final BigDecimal amount;

    public DebitFailedEvent(String customerId, String accountBid, String message, BigDecimal amount) {
        super(customerId, accountBid, message);
        this.amount = amount;
    }
}
