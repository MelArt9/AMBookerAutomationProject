package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.ApiClient;
import core.models.Booking;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class GetBookingByIdTest {

    private ApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
    }

    @Test
    public void getBookingById() throws IOException {
        Response response = apiClient.getBooking();
        assertEquals(200, response.getStatusCode());

        String responseBody = response.getBody().asString();

        Booking[] bookings = objectMapper.readValue(responseBody, Booking[].class);

        assertThat(bookings, notNullValue());
        assertNotEquals(0, bookings.length);

        int bookingId = bookings[0].getBookingid();

        Response getResponseBookingId = apiClient.getBookingById(bookingId);
        assertEquals(200, getResponseBookingId.getStatusCode());

        NewBooking details = getResponseBookingId.as(NewBooking.class);

        assertThat(details, notNullValue());

        assertFalse(details.getFirstname().isBlank());
        assertFalse(details.getLastname().isBlank());
    }
}
