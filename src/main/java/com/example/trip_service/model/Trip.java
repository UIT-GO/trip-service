package com.example.trip_service.model;

import com.example.trip_service.ENUM.TripStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Trip {
    @Id
    private String id;
    private String driverId;
    private String userId;
    private String origin;
    private String destination;
    private TripStatus status;
}
