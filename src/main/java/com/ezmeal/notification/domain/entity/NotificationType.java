package com.ezmeal.notification.domain.entity;

public enum NotificationType {
    // user
    USER_CREATED,
    // company
    COMPANY_CREATED, COMPANY_DELETED,
    // product
    PRODUCT_CREATED, PRODUCT_DELETED, DAILY_MENU_CREATED,
    // order
    ORDER_STATUS_CHANGED, ORDER_REVIEWED,
    // shipment
    SHIPMENT_STARTED, SHIPMENT_DELIVERED,
    // cs
    CS_CREATED, CS_UPDATED, CS_ANSWERED,
    // payment
    PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_CANCELLED,
    // admin
    ADMIN_BROADCAST
}
