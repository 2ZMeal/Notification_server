package com.ezmeal.notification.domain.event;

public interface CsEventConsumer {
    void onCsCreated(String message);
    void onCsUpdated(String message);
    void onCsAnswered(String message);
}
