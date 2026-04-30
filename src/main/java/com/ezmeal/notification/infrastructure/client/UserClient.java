package com.ezmeal.notification.infrastructure.client;

import com.ezmeal.notification.infrastructure.client.dto.UserApiResponse;
import com.ezmeal.notification.infrastructure.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/v1/users/{userId}")
    UserApiResponse<UserResponse> getUser(@PathVariable("userId") UUID userId);
}
