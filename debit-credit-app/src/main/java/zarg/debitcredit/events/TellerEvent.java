package zarg.debitcredit.events;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
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
}
