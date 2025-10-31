package com.travelmaster.trip.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Trip Service E2E Tests")
class TripE2ETest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @Order(1)
    @DisplayName("E2E: User searches for trips")
    void testSearchTrips() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("origin", "Moscow")
            .queryParam("destination", "Paris")
            .queryParam("passengers", 2)
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/trips/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    @DisplayName("E2E: User views trip details")
    void testViewTripDetails() {
        // First search for trips to get a valid ID
        Integer tripId = given()
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 1)
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(200)
            .extract()
            .path("content[0].id");

        if (tripId != null) {
            // Then view trip details
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/v1/trips/" + tripId)
            .then()
                .statusCode(200)
                .body("id", equalTo(tripId))
                .body("title", notNullValue())
                .body("origin", notNullValue())
                .body("destination", notNullValue());
        }
    }

    @Test
    @Order(3)
    @DisplayName("E2E: User filters trips by price range")
    void testFilterByPriceRange() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("minPrice", 10000)
            .queryParam("maxPrice", 100000)
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/trips/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: User gets 404 for non-existent trip")
    void testNonExistentTrip() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/trips/999999")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: User lists all available trips")
    void testListAllTrips() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 20)
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("page", equalTo(0))
            .body("size", equalTo(20));
    }
}

