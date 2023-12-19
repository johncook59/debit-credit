package zarg.debitcredit.events;

import java.math.BigDecimal;

public class DebitEvent extends TellerEvent {
    private final BigDecimal amount;

    public DebitEvent(String customerId, String accountBid, BigDecimal amount) {
        super(customerId, accountBid, "");
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new StringBuilder("DebitEvent{")
                .append("customerId: ").append(getCustomerId()).append(", ")
                .append("accountId: ").append(getAccountId()).append(", ")
                .append("amount: ").append(amount).append(", ")
                .append("timestamp: ").append(getTimestamp())
                .append('}')
                .toString();
    }
}
