package zarg.debitcredit.events;

import java.math.BigDecimal;

public class CreditFailedEvent extends TellerEvent {
    private final BigDecimal amount;

    public CreditFailedEvent(String customerId, String accountBid, String message, BigDecimal amount) {
        super(customerId, accountBid, message);
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new StringBuilder("CreditFailedEvent{")
                .append("customerId: ").append(getCustomerId()).append(", ")
                .append("accountId: ").append(getAccountId()).append(", ")
                .append("amount: ").append(amount).append(", ")
                .append("timestamp: ").append(getTimestamp()).append(", ")
                .append("message: ").append(getMessage())
                .append('}')
                .toString();
    }
}
