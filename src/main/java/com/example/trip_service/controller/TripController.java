package com.example.trip_service.controller;

import com.example.trip_service.ENUM.TripStatus;
import com.example.trip_service.request.TripRequest;
import com.example.trip_service.response.TripResponse;
import com.example.trip_service.service.TripService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public String createTrip(@RequestBody TripRequest tripRequest) throws JsonProcessingException {
        return tripService.createTrip(tripRequest);
    }

    @GetMapping("/{tripId}/status")
    public String getTripStatus(@PathVariable String tripId) {
        return tripService.getTripStatus(tripId);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTripDetails(@PathVariable String tripId) {
        return tripService.getTripDetails(tripId);
    }

    @PutMapping("/{tripId}/status")
    public String updateTripStatus(@PathVariable String tripId, @RequestParam TripStatus status) {
        return tripService.updateTripStatus(tripId, status);
    }
}
