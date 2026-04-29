package com.ezmeal.notification.domain.event;

public interface ProductEventConsumer {
    void onProductCreated(String message);
    void onProductDeleted(String message);
    void onDailyMenuCreated(String message);
}
