package com.ezmeal.notification.domain.event;

public interface ShipmentEventConsumer {
    void onShipmentStarted(String message);
    void onShipmentDelivered(String message);
}
