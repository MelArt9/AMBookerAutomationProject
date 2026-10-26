package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.ApiClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchBookingTest {
    private ApiClient apiClient;
    private ObjectMapper objectMapper;
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
    @DisplayName("Частичное обновление бронирования")
    @Description("Сценарий 5 из файла с заданием: POST /booking, PATCH /booking/{id}, DELETE /booking/{id}")
    public void patchBookingIdTest() throws  JsonProcessingException {
        Map<String, Object> patchPayload = new HashMap<>();
        patchPayload.put("firstname", "Vsevolod");

        String requestBody = objectMapper.writeValueAsString(patchPayload);

        apiClient.createToken("admin", "password123");

        Response updatedResponse = apiClient.patchBooking(bookingId, requestBody);
        assertEquals(200, updatedResponse.getStatusCode());

        Response getResponse = apiClient.getBookingById(bookingId);
        assertEquals(200, getResponse.getStatusCode());

        NewBooking getBooking = getResponse.as(NewBooking.class);
        assertNotNull(getBooking);

        assertEquals("Vsevolod", getBooking.getFirstname());
        assertEquals(newBooking.getLastname(), getBooking.getLastname());
        assertEquals(newBooking.getTotalprice(), getBooking.getTotalprice());
        assertEquals(newBooking.isDepositpaid(), getBooking.isDepositpaid());
        assertEquals(newBooking.getBookingdates().getCheckin(), getBooking.getBookingdates().getCheckin());
        assertEquals(newBooking.getBookingdates().getCheckout(), getBooking.getBookingdates().getCheckout());
        assertEquals(newBooking.getAdditionalneeds(), getBooking.getAdditionalneeds());
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