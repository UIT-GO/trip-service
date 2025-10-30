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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripServiceImplTest {
    @Mock
    private TripRepository tripRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private TripServiceImpl tripService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTrip() throws JsonProcessingException {
        TripRequest request = new TripRequest();
        request.setDestination("Destination");
        request.setOrigin("Origin");
        request.setUserId("user1");
        request.setLatitude(String.valueOf(10.0));
        request.setLongitude(String.valueOf(20.0));

        Trip trip = new Trip();
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        String result = tripService.createTrip(request);
        assertEquals("Waiting for driver to accept the trip", result);
        verify(tripRepository, times(1)).save(any(Trip.class));
        verify(kafkaTemplate, times(1)).send(eq("trip_create_wait_driver"), anyString());
    }

    @Test
    void testGetTripStatus() {
        Trip trip = new Trip();
        trip.setStatus(TripStatus.PENDING);
        when(tripRepository.findById("trip1")).thenReturn(Optional.of(trip));

        String status = tripService.getTripStatus("trip1");
        assertEquals("PENDING", status);
    }

    @Test
    void testGetTripStatusNotFound() {
        when(tripRepository.findById("trip2")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tripService.getTripStatus("trip2"));
        assertTrue(ex.getMessage().contains("Trip not found with id: trip2"));
    }

    @Test
    void testGetTripDetails() {
        Trip trip = new Trip();
        trip.setId("trip1");
        trip.setOrigin("Origin");
        trip.setDestination("Destination");
        trip.setStatus(TripStatus.PENDING);
        trip.setUserId("user1");
        trip.setDriverId("driver1");
        when(tripRepository.findById("trip1")).thenReturn(Optional.of(trip));
        UserDriverNameDTO dto = new UserDriverNameDTO();
        dto.setUserName("UserName");
        dto.setDriverName("DriverName");
        when(userClient.getUserDriverName("user1", "driver1")).thenReturn(dto);

        TripResponse response = tripService.getTripDetails("trip1");
        assertEquals("trip1", response.getId());
        assertEquals("Origin", response.getOrigin());
        assertEquals("Destination", response.getDestination());
        assertEquals("PENDING", response.getStatus());
        assertEquals("UserName", response.getUserName());
        assertEquals("DriverName", response.getDriverName());
    }

    @Test
    void testGetTripDetailsNotFound() {
        when(tripRepository.findById("trip2")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tripService.getTripDetails("trip2"));
        assertTrue(ex.getMessage().contains("Trip not found with id: trip2"));
    }

    @Test
    void testUpdateTripStatus() {
        Trip trip = new Trip();
        trip.setStatus(TripStatus.PENDING);
        when(tripRepository.findById("trip1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        String result = tripService.updateTripStatus("trip1", TripStatus.COMPLETED);
        assertEquals("Trip status updated to COMPLETED", result);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        verify(tripRepository, times(1)).save(trip);
    }

    @Test
    void testUpdateTripStatusNotFound() {
        when(tripRepository.findById("trip2")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tripService.updateTripStatus("trip2", TripStatus.COMPLETED));
        assertTrue(ex.getMessage().contains("Trip not found with id: trip2"));
    }
}

