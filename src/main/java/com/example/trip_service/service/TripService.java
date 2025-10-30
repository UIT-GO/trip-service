package com.example.trip_service.service;

import com.example.trip_service.ENUM.TripStatus;
import com.example.trip_service.request.TripRequest;
import com.example.trip_service.response.TripResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface TripService {
    String createTrip(TripRequest tripRequest) throws JsonProcessingException;
    String getTripStatus(String tripId);
    TripResponse getTripDetails(String tripId);
    String updateTripStatus(String tripId, TripStatus status);
}
