package com.example.trip_service.client;

import com.example.trip_service.DTO.UserDTO;
import com.example.trip_service.DTO.UserDriverNameDTO;
import com.example.trip_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:3030",
        path = "/api/users",
        configuration = FeignConfig.class
)
public interface UserClient {
    @GetMapping("/me")
    UserDTO getUserInfo();

    @GetMapping("/{userId}/{driverId}")
    UserDriverNameDTO getUserDriverName(@PathVariable String userId, @PathVariable String driverId);
}
