package com.ezmeal.notification.domain.event;

public interface UserEventConsumer {
    void onUserCreated(String message);
}
