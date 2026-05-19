package com.ezmeal.notification.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.notification.domain.event.payload.UserCreatedPayload;

public interface UserEventConsumer {
    void onUserCreated(EventEnvelope<UserCreatedPayload> event);
}
