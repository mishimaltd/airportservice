package com.mishima.tripbuddy.airportservice

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scala.concurrent.duration._

class AirportServiceSimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http // 4
    .baseURL("https://airportservice-tripbuddy.herokuapp.com")
    .acceptHeader("application/json")

  val scn: ScenarioBuilder = scenario("Load Airport")
    .exec(http("Load page")
        .get("/airports/code/RDU")
        .check(status.is(200)))

  setUp(scn.inject(
    nothingFor(4 seconds),
    atOnceUsers(10),
    rampUsers(30) over (5 seconds),
    constantUsersPerSec(35) during (90 seconds) randomized))
    .protocols(httpConf)
    .assertions(
      global.failedRequests.count.is(0),
      global.responseTime.percentile3.lte(1200), //check that 95% of the response time is under 1200ms
      global.responseTime.percentile4.lte(800)
    )

}
