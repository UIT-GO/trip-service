package com.example.trip_service.response;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class TripResponse {
    @Id
    private String id;
    private String driverName;
    private String userName;
    private String origin;
    private String destination;
    private String status;
}
