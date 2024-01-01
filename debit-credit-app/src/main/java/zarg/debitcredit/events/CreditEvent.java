package zarg.debitcredit.events;

import java.math.BigDecimal;

public class CreditEvent extends TellerEvent {
    private final BigDecimal amount;

    public CreditEvent(String customerId, String accountBid, BigDecimal amount) {
        super(customerId, accountBid, "");
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new StringBuilder("CreditEvent{")
                .append("customerId: ").append(getCustomerId()).append(", ")
                .append("accountId: ").append(getAccountId()).append(", ")
                .append("amount: ").append(amount).append(", ")
                .append("timestamp: ").append(getTimestamp())
                .append('}')
                .toString();
    }
}
