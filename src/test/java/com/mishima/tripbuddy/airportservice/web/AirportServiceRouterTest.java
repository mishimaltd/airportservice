package com.mishima.tripbuddy.airportservice.web;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.tripbuddy.airportservice.entity.Airport;
import com.mishima.tripbuddy.airportservice.repositories.ReactiveAirportRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class AirportServiceRouterTest {

    @Autowired
    private ReactiveAirportRepository repository;

    @Autowired
    private ReactiveMongoOperations operations;

    @Autowired
    private WebTestClient webTestClient;

    @Before
    public void setUp() {
        operations.collectionExists(Airport.class)
                .flatMap(exists -> exists ? operations.dropCollection(Airport.class) : Mono.just(exists))
                .flatMap(o -> operations.createCollection(Airport.class)
                        .then(operations.indexOps(Airport.class).ensureIndex(new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE))))
                .then().block();

        repository.saveAll(Flux.just(
                Airport.builder().name("Raleigh").cityId("RAL").code("RDU").location(new double[]{35.8861980, -79.0601160}).build(),
                Airport.builder().name("Washington").cityId("WAS").code("WAS").location(new double[]{38.953116, -77.456539}).build()))
                .then().block();
    }

    @After
    public void tearDown() {
        operations.dropCollection(Airport.class);
    }

    @Test
    public void testGetByCode() throws Exception {
        String result = webTestClient
            .get()
            .uri("/airports/code/{code}", "RDU")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String.class).returnResult().getResponseBody();
        Airport airport = new ObjectMapper().readValue(result, new TypeReference<Airport>(){});
        assertNotNull(airport);
        assertEquals("RDU", airport.getCode());
    }


}
