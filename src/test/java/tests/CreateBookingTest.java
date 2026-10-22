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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBookingTest {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();

        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2025-01-10", "2025-01-15"));
        newBooking.setAdditionalneeds("Breakfast");
    }

    @Test
    @DisplayName("Создание бронирования с корректными данными")
    @Description("Сценарий 1 из файла с заданием: POST /booking, проверка ответа, удаление")
    public void createBookingWithValidData() throws JsonProcessingException {
        // Шаг 1. Объект -> JSON-строка (сериализация)
        String requestBody = objectMapper.writeValueAsString(newBooking);

        // Шаг 2. Отправляем запрос и проверяем код ответа
        Response response = apiClient.createBooking(requestBody);
        assertEquals(200, response.getStatusCode());

        // Шаг 3. JSON-ответ -> объект (десериализация)
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);
        assertThat(createdBooking, notNullValue());

        // Шаг 4. Сверяем: то, что создалось, совпадает с тем, что отправляли.
        assertEquals(newBooking.getFirstname(), createdBooking.getBooking().getFirstname());
        assertEquals(newBooking.getLastname(), createdBooking.getBooking().getLastname());
        assertEquals(newBooking.getTotalprice(), createdBooking.getBooking().getTotalprice());
        assertEquals(newBooking.isDepositpaid(), createdBooking.getBooking().isDepositpaid());
        assertEquals(newBooking.getBookingdates().getCheckin(),
                createdBooking.getBooking().getBookingdates().getCheckin());
        assertEquals(newBooking.getBookingdates().getCheckout(),
                createdBooking.getBooking().getBookingdates().getCheckout());
        assertEquals(newBooking.getAdditionalneeds(), createdBooking.getBooking().getAdditionalneeds());
    }

    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");

        if (createdBooking != null) {
            int bookingId = createdBooking.getBookingid();
            apiClient.deleteBooking(bookingId, 201);
            apiClient.getBookingById(bookingId, 404);
        }
    }
}
