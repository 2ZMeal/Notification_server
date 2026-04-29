package com.ezmeal.notification.domain.event;

public interface OrderEventConsumer {
    void onOrderStatus(String message);
    void onOrderReviewed(String message);
}
