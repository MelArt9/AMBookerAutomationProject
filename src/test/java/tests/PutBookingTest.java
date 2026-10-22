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

import static org.junit.jupiter.api.Assertions.*;

public class PutBookingTest {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking, updatedBooking;
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
    @DisplayName("Обновление бронирования")
    @Description("Сценарий 4 из файла с заданием: POST /booking, PUT /booking/{id}, DELETE /booking/{id}")
    public void putBookingIdTest() throws  JsonProcessingException {
        updatedBooking = new NewBooking();
        updatedBooking.setFirstname("Roman");
        updatedBooking.setLastname(newBooking.getLastname());
        updatedBooking.setTotalprice(999);
        updatedBooking.setDepositpaid(newBooking.isDepositpaid());
        updatedBooking.setBookingdates(newBooking.getBookingdates());
        updatedBooking.setAdditionalneeds(newBooking.getAdditionalneeds());

        String requestBody = objectMapper.writeValueAsString(updatedBooking);

        apiClient.createToken("admin", "password123");

        Response updatedResponse = apiClient.putBooking(bookingId, requestBody);
        assertEquals(200, updatedResponse.getStatusCode());

        Response getResponse = apiClient.getBookingById(bookingId);
        assertEquals(200, getResponse.getStatusCode());

        NewBooking getBooking = getResponse.as(NewBooking.class);
        assertNotNull(getBooking);

        assertEquals(updatedBooking.getFirstname(), getBooking.getFirstname());
        assertEquals(updatedBooking.getTotalprice(), getBooking.getTotalprice());
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
