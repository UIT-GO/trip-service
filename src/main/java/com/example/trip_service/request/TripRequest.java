package com.example.trip_service.request;

import lombok.Data;

@Data
public class TripRequest {
    private String userId;
    private String origin;
    private String destination;
    private String latitude;
    private String longitude;
}
