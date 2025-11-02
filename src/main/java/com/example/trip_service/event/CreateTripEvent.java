package com.example.trip_service.event;

import lombok.Data;

@Data
public class CreateTripEvent {
    private String tripId;
    private String userId;
    private String origin;
    private String destination;
    private String latitude;
    private String longitude;
}
