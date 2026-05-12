package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.OrderReviewedPayload;
import com.ezmeal.notification.domain.event.payload.OrderStatusPayload;

public interface OrderEventConsumer {
    void onOrderStatus(EventEnvelope<OrderStatusPayload> event);
    void onOrderReviewed(EventEnvelope<OrderReviewedPayload> event);
}
