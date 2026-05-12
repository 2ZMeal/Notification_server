package com.ezmeal.notification.domain.event.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ezmeal.common.message.DomainEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CompanyCreatedPayload implements DomainEvent {
    private UUID companyId;
    private UUID userId;
    private String companyName;
}
