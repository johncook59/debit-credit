package zarg.debitcredit.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class LoggingTellerEventListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void handleCommittedEvent(TellerEvent event) {
        log.info("Committed {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    void handleRolledBackEvent(TellerEvent event) {
        log.error("Rolled back transaction for {}", event);
    }
}