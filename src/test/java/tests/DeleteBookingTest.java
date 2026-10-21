package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.ApiClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class DeleteBookingTest {

    private ApiClient apiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
    }

    @Test
    public void deleteBookingTest() throws Exception{
        Response response = apiClient.getBooking();

        String responseBody = response.getBody().asString();

        Booking[] bookings = objectMapper.readValue(responseBody, Booking[].class);

        assertThat(bookings, notNullValue());
        assertNotEquals(0, bookings.length);

        int bookingId = bookings[0].getBookingid();

        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(bookingId);
        apiClient.getBookingById(bookingId, 404);
    }

    @Test
    public void deleteBookingWithoutTokenTest() throws Exception{
        Response response = apiClient.getBooking();

        String responseBody = response.getBody().asString();

        Booking[] bookings = objectMapper.readValue(responseBody, Booking[].class);

        assertThat(bookings, notNullValue());
        assertNotEquals(0, bookings.length);

        int bookingId = bookings[0].getBookingid();

        apiClient.deleteBooking(bookingId, 403);
        apiClient.getBookingById(bookingId, 200);
    }
}
