package com.example.trip_service.event;

import lombok.Data;

@Data
public class AcceptTripEvent {
    private String tripId;
    private String driverId;
}
