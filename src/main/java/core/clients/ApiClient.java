package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiClient {

    private final String baseUrl;
    private String token;

    public ApiClient() {
        this.baseUrl = determineBaseUrl();
    }

    public void createToken(String username, String password) {
        String body = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        Response response = getRequestSpec()
                .body(body)
                .when()
                .post(ApiEndpoints.AUTH.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();

        token = response.jsonPath().getString("token");
    }

    private Filter addAuthTokenFilter() {
        return (requestSpec, responseSpec, ctx) -> {
            if (token != null) {
                requestSpec.cookie("token", token);
            }

            return ctx.next(requestSpec, responseSpec);
        };
    }

    private RequestSpecification getRequestSpec() {
        return RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter());
    }

    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath())
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    public Response getBooking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response getBookingById(int bookingId) {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/" + bookingId)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response getBookingById(int bookingId, int expectedStatusCode) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .response();
    }

    public Response deleteBooking(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .response();
    }

    public Response deleteBooking(int bookingId, int expectedStatusCode) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(expectedStatusCode)
                .extract()
                .response();
    }

    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration file: " + configFileName, e);
        }
        return properties.getProperty("base.url");
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}