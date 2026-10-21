package tests;

import core.clients.ApiClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckTest {

    private ApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
    }

    @Test
    public void pingShouldReturn201() {
        Response response = apiClient.ping();
        assertEquals(201, response.getStatusCode());
    }
}
