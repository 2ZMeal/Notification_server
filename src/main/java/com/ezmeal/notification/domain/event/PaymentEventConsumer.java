package com.ezmeal.notification.domain.event;

public interface PaymentEventConsumer {
    void onPaymentSuccess(String message);
    void onPaymentFailed(String message);
    void onPaymentCancelled(String message);
}
