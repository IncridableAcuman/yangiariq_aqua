package com.aquaai.wateraccounting.domain.port;

import java.util.UUID;

public interface AccountingNotificationPort {

    void sendReconciliationAlert(UUID reconciliationId, String title, String message, String severity);
}
