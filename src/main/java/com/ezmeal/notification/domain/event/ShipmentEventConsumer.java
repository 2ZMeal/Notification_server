package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.notification.domain.event.payload.ShipmentStartedPayload;

public interface ShipmentEventConsumer {
    void onShipmentStarted(EventEnvelope<ShipmentStartedPayload> event);
    void onShipmentDelivered(EventEnvelope<ShipmentDeliveredPayload> event);
}
