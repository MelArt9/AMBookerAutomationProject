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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FilterBookingTest {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private int[] bookingIds = new int[3];
    private String nameSuffix;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        nameSuffix = String.valueOf(System.currentTimeMillis());

        bookingIds[0] = createBooking("John", "Doe", "2099-01-10", "2099-01-15");
        bookingIds[1] = createBooking("John", "Smith", "2099-02-10", "2099-02-15");
        bookingIds[2] = createBooking("Jane", "Doe", "1899-01-10", "1899-01-15");
    }

    private int createBooking(String firstname, String lastname, String checkin, String checkout)
            throws JsonProcessingException {
        NewBooking newBooking = new NewBooking();
        newBooking.setFirstname(firstname + nameSuffix);
        newBooking.setLastname(lastname + nameSuffix);
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates(checkin, checkout));
        newBooking.setAdditionalneeds("Breakfast");

        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);
        int bookingId = response.as(CreatedBooking.class).getBookingid();
        assertTrue(bookingId > 0);
        return bookingId;
    }

    @ParameterizedTest(name = "[{index}] firstname={0}, lastname={1}, checkin={2}, checkout={3}")
    @CsvSource({
            "John, , , , true, true, false",
            ", Doe, , , true, false, true",
            ", , 2099-01-01, , true, true, false",
            ", , , 1900-01-01, false, false, true",
            "John, Doe, , , true, false, false",
            "John, , 2099-01-01, , true, true, false",
            "John, , , 2099-02-01, true, false, false",
            ", Doe, 2099-01-01, , true, false, false",
            ", Doe, , 2099-02-01, true, false, true",
            ", , 2099-01-01, 2099-02-01, true, false, false",
            "John, Doe, 2099-01-01, , true, false, false",
            "John, Doe, , 2099-02-01, true, false, false",
            "John, , 2099-01-01, 2099-02-01, true, false, false",
            ", Doe, 2099-01-01, 2099-02-01, true, false, false",
            "John, Doe, 2099-01-01, 2099-02-01, true, false, false"
    })
    @DisplayName("Фильтрация бронирований по имени, фамилии и датам")
    @Description("Сценарий 6: создание бронирований, GET /booking с комбинациями фильтров, удаление")
    public void filterBookingTest(String firstname, String lastname, String checkin, String checkout,
                                  boolean firstFound, boolean secondFound, boolean thirdFound) {
        Map<String, String> filters = new HashMap<>();
        if (firstname != null) {
            firstname = firstname + nameSuffix;
            filters.put("firstname", firstname);
        }
        if (lastname != null) {
            lastname = lastname + nameSuffix;
            filters.put("lastname", lastname);
        }
        if (checkin != null) {
            filters.put("checkin", checkin);
        }
        if (checkout != null) {
            filters.put("checkout", checkout);
        }

        Response response = apiClient.getBooking(filters);
        assertEquals(200, response.getStatusCode());

        List<Integer> foundIds = response.jsonPath().getList("bookingid");
        assertNotNull(foundIds);
        assertEquals(firstFound, foundIds.contains(bookingIds[0]));
        assertEquals(secondFound, foundIds.contains(bookingIds[1]));
        assertEquals(thirdFound, foundIds.contains(bookingIds[2]));

        for (int bookingId : foundIds) {
            assertTrue(bookingId > 0);
            NewBooking details = apiClient.getBookingById(bookingId).as(NewBooking.class);

            if (firstname != null) {
                assertEquals(firstname, details.getFirstname());
            }
            if (lastname != null) {
                assertEquals(lastname, details.getLastname());
            }
            if (checkin != null) {
                assertTrue(LocalDate.parse(details.getBookingdates().getCheckin())
                        .isAfter(LocalDate.parse(checkin)));
            }
            if (checkout != null) {
                assertTrue(LocalDate.parse(details.getBookingdates().getCheckout())
                        .isBefore(LocalDate.parse(checkout)));
            }
        }
    }

    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");

        for (int bookingId : bookingIds) {
            if (bookingId > 0) {
                apiClient.deleteBooking(bookingId, 201);
                apiClient.getBookingById(bookingId, 404);
            }
        }
    }
}
