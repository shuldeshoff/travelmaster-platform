package com.travelmaster.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BookingServiceStressTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8084") // Booking Service URL
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val getBookingScenario = scenario("Get User Bookings")
    .exec(http("Get Bookings by User ID")
      .get("/api/v1/bookings/user/100")
      .queryParam("page", "0")
      .queryParam("size", "10")
      .check(status.is(200)))
    .pause(1)

  val viewBookingDetailsScenario = scenario("View Booking Details")
    .exec(http("Get Booking by ID")
      .get("/api/v1/bookings/1")
      .check(status.in(200, 404)))
    .pause(1)

  // Stress Test: gradually increase load to find breaking point
  setUp(
    getBookingScenario.inject(
      rampUsers(100) during (1.minutes),
      constantUsersPerSec(20) during (2.minutes),
      rampUsers(200) during (1.minutes),
      constantUsersPerSec(50) during (2.minutes)
    ),
    viewBookingDetailsScenario.inject(
      rampUsers(50) during (1.minutes),
      constantUsersPerSec(10) during (2.minutes),
      rampUsers(100) during (1.minutes),
      constantUsersPerSec(25) during (2.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile3.lt(5000),
     global.successfulRequests.percent.gt(90)
   )
}

