package com.ezmeal.notification.domain.event;

public interface CompanyEventConsumer {
    void onCompanyCreated(String message);
    void onCompanyDeleted(String message);
}
