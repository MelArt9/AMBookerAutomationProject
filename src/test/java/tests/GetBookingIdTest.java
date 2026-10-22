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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class GetBookingIdTest {

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
    @DisplayName("Поиск и проверка бронирования по ID")
    @Description("Сценарий 3 из файла с заданием: POST /booking, GET /booking/{id}, DELETE /booking/{id}")
    public void getBookingIdTest() {
        Response getResponseBookingId = apiClient.getBookingById(bookingId);
        assertEquals(200, getResponseBookingId.getStatusCode());

        NewBooking details = getResponseBookingId.as(NewBooking.class);
        assertThat(details, notNullValue());

        assertEquals(newBooking.getFirstname(), details.getFirstname());
        assertEquals(newBooking.getLastname(), details.getLastname());
        assertEquals(newBooking.getTotalprice(), details.getTotalprice());
        assertEquals(newBooking.isDepositpaid(), details.isDepositpaid());
        assertEquals(newBooking.getBookingdates().getCheckin(), details.getBookingdates().getCheckin());
        assertEquals(newBooking.getBookingdates().getCheckout(), details.getBookingdates().getCheckout());
        assertEquals(newBooking.getAdditionalneeds(), details.getAdditionalneeds());
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