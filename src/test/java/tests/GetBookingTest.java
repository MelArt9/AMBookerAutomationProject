package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.ApiClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GetBookingTest {
    private ApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
    }

    @Test
    public void getBookingShouldReturnListOfIds() throws JsonProcessingException {
        Response response = apiClient.getBooking();
        assertEquals(200, response.getStatusCode());

        String responseBody = response.getBody().asString();

        List<Booking> bookings = objectMapper.readValue(
                responseBody, new TypeReference<List<Booking>>() {});

        assertFalse(bookings.isEmpty());

        for (Booking booking : bookings) {
            assertTrue(booking.getBookingid() > 0);
        }
    }
}