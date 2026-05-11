package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.CompanyCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CompanyDeletedPayload;

public interface CompanyEventConsumer {
    void onCompanyCreated(EventEnvelope<CompanyCreatedPayload> event);
    void onCompanyDeleted(EventEnvelope<CompanyDeletedPayload> event);
}
