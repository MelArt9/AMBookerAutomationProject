package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.ApiClient;
import core.models.Booking;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GetListBookingTest {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private int bookingId;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();

        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2025-01-10", "2025-01-15"));
        newBooking.setAdditionalneeds("Breakfast");

        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response createResponse = apiClient.createBooking(requestBody);
        assertEquals(200, createResponse.getStatusCode());

        bookingId = createResponse.as(CreatedBooking.class).getBookingid();
        assertTrue(bookingId > 0);
    }

    @Test
    @DisplayName("Проверка списка всех бронирований")
    @Description("Сценарий 2 из файла с заданием: POST /booking, GET /booking, DELETE /booking/{id}")
    public void getListBookingsTest() throws JsonProcessingException {
        Response response = apiClient.getBooking();
        assertEquals(200, response.getStatusCode());

        String responseBody = response.getBody().asString();

        Booking[] bookings = objectMapper.readValue(responseBody, Booking[].class);

        assertNotNull(bookings);
        assertNotEquals(0, bookings.length);

        for (Booking booking : bookings) {
            assertTrue(booking.getBookingid() > 0);
        }
    }

    @AfterEach
    public void tearDown() {
        if (bookingId > 0) {
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(bookingId, 201);
            apiClient.getBookingById(bookingId, 404);
        }
    }
}