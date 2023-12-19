package zarg.debitcredit.events;

import java.time.Instant;

public abstract class TellerEvent {
    private final String customerId;
    private final String accountId;
    private final String message;
    private final Instant timestamp;

    protected TellerEvent(String customerId, String accountId, String message) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
