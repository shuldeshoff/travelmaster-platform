package com.travelmaster.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class TripServiceLoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8083") // Trip Service URL
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val searchTripsScenario = scenario("Search Trips")
    .exec(http("Search Moscow to Paris")
      .get("/api/v1/trips/search")
      .queryParam("origin", "Moscow")
      .queryParam("destination", "Paris")
      .queryParam("passengers", "2")
      .queryParam("page", "0")
      .queryParam("size", "10")
      .check(status.is(200)))
    .pause(1)

  val viewTripDetailsScenario = scenario("View Trip Details")
    .exec(http("Get Trip by ID")
      .get("/api/v1/trips/1")
      .check(status.is(200)))
    .pause(1)

  val listAllTripsScenario = scenario("List All Trips")
    .exec(http("Get All Trips")
      .get("/api/v1/trips")
      .queryParam("page", "0")
      .queryParam("size", "20")
      .check(status.is(200)))
    .pause(2)

  setUp(
    searchTripsScenario.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(10) during (1.minutes)
    ),
    viewTripDetailsScenario.inject(
      rampUsers(30) during (30.seconds),
      constantUsersPerSec(5) during (1.minutes)
    ),
    listAllTripsScenario.inject(
      rampUsers(20) during (30.seconds),
      constantUsersPerSec(3) during (1.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(3000),
     global.successfulRequests.percent.gt(95)
   )
}

