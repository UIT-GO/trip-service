package com.example.trip_service.service;

import com.example.trip_service.DTO.UserDriverNameDTO;
import com.example.trip_service.ENUM.TripStatus;
import com.example.trip_service.client.UserClient;
import com.example.trip_service.event.CreateTripEvent;
import com.example.trip_service.model.Trip;
import com.example.trip_service.repository.TripRepository;
import com.example.trip_service.request.TripRequest;
import com.example.trip_service.response.TripResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TRIP_CREATED_TOPIC = "trip_create_wait_driver";
    private final UserClient userClient;

    public TripServiceImpl(TripRepository tripRepository, KafkaTemplate<String, String> kafkaTemplate, UserClient userClient) {
        this.tripRepository = tripRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.userClient = userClient;
    }

    @Override
    public String createTrip(TripRequest tripRequest) throws JsonProcessingException {
        Trip trip = new Trip();
        trip.setDestination(tripRequest.getDestination());
        trip.setStatus(TripStatus.PENDING);
        trip.setOrigin(tripRequest.getOrigin());
        trip.setUserId(tripRequest.getUserId());
        tripRepository.save(trip);

        //Create a trip created event and publish to kafka
        CreateTripEvent createTripEvent = new CreateTripEvent();
        createTripEvent.setDestination(tripRequest.getDestination());
        createTripEvent.setLatitude(tripRequest.getLatitude());
        createTripEvent.setLongitude(tripRequest.getLongitude());
        createTripEvent.setOrigin(tripRequest.getOrigin());
        createTripEvent.setUserId(tripRequest.getUserId());
        createTripEvent.setTripId(trip.getId());

        String json = new ObjectMapper().writeValueAsString(createTripEvent);
        kafkaTemplate.send(TRIP_CREATED_TOPIC, json);

        return "Waiting for driver to accept the trip";
    }

    @Override
    public String getTripStatus(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new RuntimeException("Trip not found with id: " + tripId)
        );
        return trip.getStatus().toString();
    }

    @Override
    public TripResponse getTripDetails(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new RuntimeException("Trip not found with id: " + tripId)
        );

        TripResponse tripResponse = new TripResponse();
        tripResponse.setId(trip.getId());
        tripResponse.setOrigin(trip.getOrigin());
        tripResponse.setDestination(trip.getDestination());
        tripResponse.setStatus(trip.getStatus().toString());

        UserDriverNameDTO userDriverNameDTO = userClient.getUserDriverName(trip.getUserId(), trip.getDriverId());
        tripResponse.setUserName(userDriverNameDTO.getUserName());
        tripResponse.setDriverName(userDriverNameDTO.getDriverName());

        return tripResponse;
    }

    @Override
    public String updateTripStatus(String tripId, TripStatus status) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new RuntimeException("Trip not found with id: " + tripId)
        );
        trip.setStatus(status);
        tripRepository.save(trip);

        return "Trip status updated to " + status;
    }
}
