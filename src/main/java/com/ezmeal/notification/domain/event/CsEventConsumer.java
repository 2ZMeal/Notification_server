package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.CsAnsweredPayload;
import com.ezmeal.notification.domain.event.payload.CsCreatedPayload;
import com.ezmeal.notification.domain.event.payload.CsUpdatedPayload;

public interface CsEventConsumer {
    void onCsCreated(EventEnvelope<CsCreatedPayload> event);
    void onCsUpdated(EventEnvelope<CsUpdatedPayload> event);
    void onCsAnswered(EventEnvelope<CsAnsweredPayload> event);
}
