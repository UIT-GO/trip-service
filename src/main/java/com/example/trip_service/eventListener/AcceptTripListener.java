package com.example.trip_service.eventListener;

import com.example.trip_service.ENUM.TripStatus;
import com.example.trip_service.event.AcceptTripEvent;
import com.example.trip_service.model.Trip;
import com.example.trip_service.repository.TripRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AcceptTripListener {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @KafkaListener(topics = "trip_created", groupId = "driver-service-group")
    public void listenCreatedTrip(String message) {
        try {
            // Parse the incoming message (assuming it's JSON)
            AcceptTripEvent acceptTripEvent = objectMapper.readValue(message, AcceptTripEvent.class);

            Trip trip = tripRepository.findById(acceptTripEvent.getTripId()).orElseThrow(
                    () -> new RuntimeException("Trip not found with id: " + acceptTripEvent.getTripId())
            );

            trip.setDriverId(acceptTripEvent.getDriverId());
            trip.setStatus(TripStatus.ACCEPTED);
            tripRepository.save(trip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
