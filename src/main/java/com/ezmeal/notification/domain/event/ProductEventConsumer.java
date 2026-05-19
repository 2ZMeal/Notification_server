package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.DailyMenuCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductCreatedPayload;
import com.ezmeal.notification.domain.event.payload.ProductDeletedPayload;

public interface ProductEventConsumer {
    void onProductCreated(EventEnvelope<ProductCreatedPayload> event);
    void onProductDeleted(EventEnvelope<ProductDeletedPayload> event);
    void onDailyMenuCreated(EventEnvelope<DailyMenuCreatedPayload> event);
}
