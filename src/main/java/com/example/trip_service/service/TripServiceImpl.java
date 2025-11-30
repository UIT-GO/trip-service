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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TRIP_CREATED_TOPIC = "trip_create_wait_driver";
    private static final String TRIP_LOGS_TOPIC = "trip_logs";
    private final UserClient userClient;
    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    public TripServiceImpl(TripRepository tripRepository, KafkaTemplate<String, String> kafkaTemplate, UserClient userClient) {
        this.tripRepository = tripRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.userClient = userClient;
    }

    private void logToKafka(String message) {
        // Wrap the log message in JSON format with service_name and timestamp
        String jsonLog = String.format("{\"message\":%s, \"service_name\":\"trip-service\", \"timestamp\":\"%s\"}",
                new ObjectMapper().valueToTree(message).toString(),
                java.time.Instant.now().toString());
        kafkaTemplate.send(TRIP_LOGS_TOPIC, jsonLog);
    }

    @Override
    public String createTrip(TripRequest tripRequest) throws JsonProcessingException {
        Trip trip = new Trip();
        trip.setDestination(tripRequest.getDestination());
        trip.setStatus(TripStatus.PENDING);
        trip.setOrigin(tripRequest.getOrigin());
        trip.setUserId(tripRequest.getUserId());

        logger.info("Start saving trip to repository - time: {}", java.time.Instant.now().toString());
        tripRepository.save(trip);
        logger.info("Finished saving trip to repository - time: {}", java.time.Instant.now().toString());

        logToKafka(String.format("Trip persisted: id=%s status=%s", trip.getId(), trip.getStatus()));
        //Create a trip created event and publish to kafka
        CreateTripEvent createTripEvent = new CreateTripEvent();
        createTripEvent.setDestination(tripRequest.getDestination());
        createTripEvent.setLatitude(tripRequest.getLatitude());
        createTripEvent.setLongitude(tripRequest.getLongitude());
        createTripEvent.setOrigin(tripRequest.getOrigin());
        createTripEvent.setUserId(tripRequest.getUserId());

        String json = new ObjectMapper().writeValueAsString(createTripEvent);
        kafkaTemplate.send(TRIP_CREATED_TOPIC, json);
        logToKafka(String.format("CreateTripEvent published: tripId=%s userId=%s topic=%s", trip.getId(), trip.getUserId(), TRIP_CREATED_TOPIC));
        return "Waiting for driver to accept the trip";
    }

    @Override
    public String getTripStatus(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new RuntimeException("Trip not found with id: " + tripId)
        );
        logToKafka(String.format("Trip status fetched: id=%s status=%s", tripId, trip.getStatus()));
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
        logToKafka(String.format("Trip details fetched: id=%s userId=%s driverId=%s status=%s", trip.getId(), trip.getUserId(), trip.getDriverId(), trip.getStatus()));
        return tripResponse;
    }

    @Override
    public String updateTripStatus(String tripId, TripStatus status) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new RuntimeException("Trip not found with id: " + tripId)
        );
        trip.setStatus(status);
        tripRepository.save(trip);
        logToKafka(String.format("Trip status updated: id=%s status=%s", tripId, status));
        return "Trip status updated to " + status;
    }
}
