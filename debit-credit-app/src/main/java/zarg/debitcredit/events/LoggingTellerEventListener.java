package zarg.debitcredit.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
class LoggingTellerEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void handleCommittedEvent(TellerEvent event) {
        log.debug("Committed {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    void handleRolledBackEvent(TellerEvent event) {
        log.error("Rolled back transaction for {}", event);
    }
}