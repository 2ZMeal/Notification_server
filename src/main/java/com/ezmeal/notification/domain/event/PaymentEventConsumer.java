package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.PaymentCancelledPayload;
import com.ezmeal.notification.domain.event.payload.PaymentFailedPayload;
import com.ezmeal.notification.domain.event.payload.PaymentSuccessPayload;

public interface PaymentEventConsumer {
    void onPaymentSuccess(EventEnvelope<PaymentSuccessPayload> event);
    void onPaymentFailed(EventEnvelope<PaymentFailedPayload> event);
    void onPaymentCancelled(EventEnvelope<PaymentCancelledPayload> event);
}
